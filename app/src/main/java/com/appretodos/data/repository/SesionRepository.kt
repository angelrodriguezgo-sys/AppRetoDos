package com.appretodos.data.repository

import com.appretodos.data.model.Reglas
import com.appretodos.data.model.Rol
import com.appretodos.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class SesionRepository(
    private val monedas: MonedasRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usuarios get() = db.collection("usuarios")
    private var escucha: ListenerRegistration? = null

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    /** Devuelve un mensaje de error, o null si todo salió bien. */
    suspend fun registrar(
        nombre: String,
        correo: String,
        numeroCuenta: String,
        password: String
    ): String? {
        val correoLimpio = correo.trim()
        if (nombre.isBlank()) return "Escribe tu nombre"
        if (!correoLimpio.contains("@")) return "Correo no válido"
        if (numeroCuenta.length !in 8..20 || !numeroCuenta.all { it.isDigit() }) {
            return "El número de cuenta debe tener entre 8 y 20 dígitos"
        }
        if (password.length < 6) return "La contraseña debe tener mínimo 6 caracteres"

        return try {
            val nuevo = auth.createUserWithEmailAndPassword(correoLimpio, password).await().user
                ?: return "No se pudo crear la cuenta"
            val datos = mapOf(
                "nombre" to nombre.trim(),
                "correo" to correoLimpio,
                "numeroCuenta" to numeroCuenta,
                "idioma" to "Español",
                "rol" to Rol.USUARIO.name,
                "monedaRecepcion" to "COP",
                "saldoUsd" to Reglas.SALDO_INICIAL_USD,
                "tenencias" to emptyMap<String, Double>()
            )
            try {
                usuarios.document(nuevo.uid).set(datos).await()
            } catch (e: Exception) {
                // No dejar una cuenta de Auth sin perfil en Firestore
                runCatching { nuevo.delete().await() }
                auth.signOut()
                return "No se pudo guardar tu perfil: ${e.localizedMessage}"
            }
            cargarPerfil()
        } catch (e: FirebaseAuthUserCollisionException) {
            "Ese correo ya está registrado"
        } catch (e: FirebaseAuthWeakPasswordException) {
            "La contraseña es muy débil"
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            "Correo no válido"
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    suspend fun iniciarSesion(correo: String, password: String): String? {
        val correoLimpio = correo.trim()
        if (correoLimpio.isBlank() || password.isBlank()) return "Completa correo y contraseña"
        return try {
            auth.signInWithEmailAndPassword(correoLimpio, password).await()
            cargarPerfil()
        } catch (e: FirebaseAuthInvalidUserException) {
            "Correo o contraseña incorrectos"
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            "Correo o contraseña incorrectos"
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    /** Si ya había una sesión abierta de antes, la recupera sin pedir login. */
    suspend fun restaurarSesion(): Boolean =
        auth.currentUser != null && cargarPerfil() == null

    /** Carga el perfil y se queda escuchando cambios (saldo, rol, moneda...) en tiempo real. */
    private suspend fun cargarPerfil(): String? {
        val actual = auth.currentUser ?: return "Sin sesión"
        return try {
            val ref = usuarios.document(actual.uid)
            val usuario = ref.get().await().aUsuario(actual.uid, actual.email ?: "")
            _usuarioActual.value = usuario
            monedas.cambiarMonedaLocal(usuario.monedaRecepcion)

            escucha?.remove()
            escucha = ref.addSnapshotListener { snap, error ->
                if (error != null) return@addSnapshotListener
                if (snap != null && snap.exists()) {
                    _usuarioActual.value = snap.aUsuario(actual.uid, actual.email ?: "")
                }
            }
            null
        } catch (e: Exception) {
            "Error al cargar tu perfil: ${e.localizedMessage}"
        }
    }

    fun cerrarSesion() {
        escucha?.remove()
        escucha = null
        auth.signOut()
        _usuarioActual.value = null
        monedas.cambiarMonedaLocal("COP")
    }

    suspend fun cambiarIdioma(idioma: String) {
        val uid = _usuarioActual.value?.uid ?: return
        _usuarioActual.update { it?.copy(idioma = idioma) }
        try {
            usuarios.document(uid).update("idioma", idioma).await()
        } catch (e: Exception) {
            // si falla, el cambio queda solo en pantalla
        }
    }

    /** Cambia (y guarda) la moneda en la que el usuario recibe y ve sus valores. */
    suspend fun cambiarMonedaRecepcion(codigo: String) {
        val uid = _usuarioActual.value?.uid ?: return
        monedas.cambiarMonedaLocal(codigo)
        _usuarioActual.update { it?.copy(monedaRecepcion = codigo) }
        try {
            usuarios.document(uid).update("monedaRecepcion", codigo).await()
        } catch (e: Exception) {
            // si falla, el cambio queda solo en pantalla
        }
    }
}

private fun DocumentSnapshot.aUsuario(uid: String, correoAuth: String): Usuario =
    Usuario(
        uid = uid,
        nombre = getString("nombre") ?: "",
        correo = getString("correo") ?: correoAuth,
        // Si es un usuario viejo, usa el campo "ultimosCuatro" que guardaba antes
        numeroCuenta = getString("numeroCuenta") ?: getString("ultimosCuatro") ?: "",
        idioma = getString("idioma") ?: "Español",
        rol = Rol.desde(getString("rol")),
        monedaRecepcion = getString("monedaRecepcion") ?: "COP",
        saldoUsd = getDouble("saldoUsd") ?: Reglas.SALDO_INICIAL_USD,
        tenencias = leerTenencias()
    )