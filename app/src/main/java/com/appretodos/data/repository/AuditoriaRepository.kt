package com.appretodos.data.repository

import com.appretodos.data.model.Rol
import com.appretodos.data.model.Transaccion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AuditoriaRepository(
    private val sesion: SesionRepository,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Todas las operaciones de todos los usuarios; solo se consulta si el rol es ADMIN. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val transacciones: Flow<List<Transaccion>> = sesion.usuarioActual
        .map { it?.rol == Rol.ADMIN }
        .distinctUntilChanged()
        .flatMapLatest { esAdmin ->
            if (esAdmin) todas() else flowOf(emptyList())
        }

    private fun todas(): Flow<List<Transaccion>> = callbackFlow {
        val registro = db.collection("transacciones")
            .orderBy("fechaMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.aTransaccion() } ?: emptyList())
            }
        awaitClose { registro.remove() }
    }

    /** Devuelve un mensaje de error, o null si quedó marcada. */
    suspend fun marcarAuditada(id: String): String? {
        val admin = sesion.usuarioActual.value ?: return "Sin sesión"
        if (admin.rol != Rol.ADMIN) return "Solo el administrador puede auditar"
        return try {
            db.collection("transacciones").document(id).update(
                mapOf(
                    "auditada" to true,
                    "auditor" to admin.correo,
                    "auditadoMillis" to System.currentTimeMillis()
                )
            ).await()
            null
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}