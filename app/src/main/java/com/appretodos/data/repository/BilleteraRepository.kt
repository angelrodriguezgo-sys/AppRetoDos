package com.appretodos.data.repository

import com.appretodos.data.model.Reglas
import com.appretodos.data.model.Rol
import com.appretodos.data.model.TipoOperacion
import com.appretodos.data.model.Transaccion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BilleteraRepository(
    private val monedas: MonedasRepository,
    private val sesion: SesionRepository,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Saldo y monedas vienen del documento del usuario, que se escucha en tiempo real.
    val saldoUsd: Flow<Double> = sesion.usuarioActual.map { it?.saldoUsd ?: 0.0 }
    val tenencias: Flow<Map<String, Double>> =
        sesion.usuarioActual.map { it?.tenencias ?: emptyMap() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val transacciones: Flow<List<Transaccion>> = sesion.usuarioActual
        .map { it?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else transaccionesDe(uid)
        }

    private fun transaccionesDe(uid: String): Flow<List<Transaccion>> = callbackFlow {
        val registro = db.collection("transacciones")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snap, _ ->
                val lista = snap?.documents
                    ?.mapNotNull { it.aTransaccion() }
                    ?.sortedByDescending { it.fechaMillis }
                    ?: emptyList()
                trySend(lista)
            }
        awaitClose { registro.remove() }
    }

    /** Compra a la tasa de mercado. Devuelve un mensaje de error, o null si salió bien. */
    suspend fun comprar(codigo: String, cantidad: Double): String? =
        operar(TipoOperacion.COMPRA, codigo, cantidad, 0.0)

    /** Venta con sobreprecio (0 a 2.5 %) sobre la tasa de mercado. */
    suspend fun vender(codigo: String, cantidad: Double, sobreprecioPct: Double): String? =
        operar(TipoOperacion.VENTA, codigo, cantidad, sobreprecioPct)

    private suspend fun operar(
        tipo: TipoOperacion,
        codigo: String,
        cantidad: Double,
        sobreprecioPct: Double
    ): String? {
        val usuario = sesion.usuarioActual.value ?: return "Sin sesión"
        if (usuario.rol != Rol.USUARIO) return "Solo los usuarios pueden comprar y vender"
        if (sobreprecioPct < 0.0 || sobreprecioPct > Reglas.LIMITE_SOBREPRECIO_PCT) {
            return "El sobreprecio debe estar entre 0 y ${Reglas.LIMITE_SOBREPRECIO_PCT} %"
        }

        val local = monedas.monedaLocal.value
        val tasa = monedas.valorEnLocal(codigo, local)          // precio de mercado por unidad
        val precio = tasa * (1.0 + sobreprecioPct / 100.0)     // precio aplicado
        val valor = cantidad * precio
        val montoUsd = valor / monedas.obtener(local).tasaUsd

        val refUsuario = db.collection("usuarios").document(usuario.uid)
        val refOperacion = db.collection("transacciones").document()

        return try {
            db.runTransaction<String?> { t ->
                val snap = t.get(refUsuario)
                val saldo = snap.getDouble("saldoUsd") ?: Reglas.SALDO_INICIAL_USD
                val tenencias = snap.leerTenencias()
                val actual = tenencias[codigo] ?: 0.0

                val error: String? = when (tipo) {
                    TipoOperacion.COMPRA ->
                        if (montoUsd > saldo + 1e-9) "Saldo insuficiente" else null
                    TipoOperacion.VENTA ->
                        if (cantidad > actual + 1e-9) "No tienes suficiente $codigo" else null
                }
                if (error != null) return@runTransaction error

                val nuevoSaldo = if (tipo == TipoOperacion.COMPRA) saldo - montoUsd else saldo + montoUsd
                val nuevaCantidad = if (tipo == TipoOperacion.COMPRA) actual + cantidad else actual - cantidad
                val nuevasTenencias = tenencias.toMutableMap().apply {
                    if (nuevaCantidad < 1e-9) remove(codigo) else put(codigo, nuevaCantidad)
                }

                t.update(
                    refUsuario,
                    mapOf("saldoUsd" to nuevoSaldo, "tenencias" to nuevasTenencias)
                )
                t.set(
                    refOperacion,
                    mapOf<String, Any>(
                        "uid" to usuario.uid,
                        "nombreUsuario" to usuario.nombre,
                        "correoUsuario" to usuario.correo,
                        "tipo" to tipo.name,
                        "codigoMoneda" to codigo,
                        "cantidad" to cantidad,
                        "tasaMercado" to tasa,
                        "precioUnitario" to precio,
                        "valor" to valor,
                        "monedaValor" to local,
                        "fechaMillis" to System.currentTimeMillis(),
                        "estado" to "EXITOSA",
                        "auditada" to false,
                        "auditor" to "",
                        "auditadoMillis" to 0L
                    )
                )
                null
            }.await()
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}