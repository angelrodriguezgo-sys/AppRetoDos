package com.appretodos.data.repository

import com.appretodos.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class SesionRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usuarios get() = db.collection("usuarios")

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    /** Devuelve un mensaje de error, o null si todo salió bien. */
    suspend fun registrar(
        nombre: String,
        correo: String,
        numeroCuenta: String,
        password: String
    ): String? {
        if (nombre.isBlank()) return "Escribe tu nombre"
        if (!correo.contains("@")) return "Correo no válido"
        if (numeroCuenta.length !in 8..20 || !numeroCuenta.all { it.isDigit() }) {
            return "El número de cuenta debe tener entre 8 y 20 dígitos"
        }
        if (password.length < 6) return "La contraseña debe tener mínimo 6 caracteres"

        return try {
            val uid = auth.createUserWithEmailAndPassword(correo, password).await().user?.uid
                ?: return "No se pudo crear la cuenta"
            val datos = mapOf(
                "nombre" to nombre,
                "correo" to correo,
                "numeroCuenta" to numeroCuenta,
                "idioma" to "Español"
            )
            usuarios.document(uid).set(datos).await()
            _usuarioActual.value = Usuario(
                nombre = nombre,
                correo = correo,
                numeroCuenta = numeroCuenta,
                idioma = "Español"
            )
            null
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
        if (correo.isBlank() || password.isBlank()) return "Completa correo y contraseña"
        return try {
            auth.signInWithEmailAndPassword(correo, password).await()
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

    private suspend fun cargarPerfil(): String? {
        val actual = auth.currentUser ?: return "Sin sesión"
        return try {
            val doc = usuarios.document(actual.uid).get().await()
            _usuarioActual.value = Usuario(
                nombre = doc.getString("nombre") ?: "",
                correo = doc.getString("correo") ?: actual.email ?: "",
                // Si es un usuario viejo, usa el campo "ultimosCuatro" que guardaba antes
                numeroCuenta = doc.getString("numeroCuenta")
                    ?: doc.getString("ultimosCuatro")
                    ?: "",
                idioma = doc.getString("idioma") ?: "Español"
            )
            null
        } catch (e: Exception) {
            "Error al cargar tu perfil: ${e.localizedMessage}"
        }
    }

    fun cerrarSesion() {
        auth.signOut()
        _usuarioActual.value = null
    }

    suspend fun cambiarIdioma(idioma: String) {
        val actual = _usuarioActual.value ?: return
        _usuarioActual.value = actual.copy(idioma = idioma)
        val uid = auth.currentUser?.uid ?: return
        try {
            usuarios.document(uid).update("idioma", idioma).await()
        } catch (e: Exception) {
            // si falla, el cambio queda solo en pantalla
        }
    }
}