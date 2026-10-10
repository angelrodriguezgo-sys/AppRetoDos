package com.appretodos.data.repository



import com.appretodos.data.model.TipoOperacion
import com.appretodos.data.model.Transaccion
import com.google.firebase.firestore.DocumentSnapshot

/** Lee el mapa "tenencias" (código -> cantidad) de un documento de usuario. */
fun DocumentSnapshot.leerTenencias(): Map<String, Double> {
    @Suppress("UNCHECKED_CAST")
    val crudas = (get("tenencias") as? Map<String, Any?>) ?: emptyMap()
    return crudas.mapNotNull { (codigo, valor) ->
        (valor as? Number)?.toDouble()?.let { codigo to it }
    }.toMap()
}

fun DocumentSnapshot.aTransaccion(): Transaccion? {
    val tipo = TipoOperacion.entries.firstOrNull { it.name == getString("tipo") } ?: return null
    return Transaccion(
        id = id,
        uid = getString("uid") ?: "",
        nombreUsuario = getString("nombreUsuario") ?: "",
        correoUsuario = getString("correoUsuario") ?: "",
        tipo = tipo,
        codigoMoneda = getString("codigoMoneda") ?: "",
        cantidad = getDouble("cantidad") ?: 0.0,
        tasaMercado = getDouble("tasaMercado") ?: 0.0,
        precioUnitario = getDouble("precioUnitario") ?: 0.0,
        valor = getDouble("valor") ?: 0.0,
        monedaValor = getString("monedaValor") ?: "",
        fechaMillis = getLong("fechaMillis") ?: 0L,
        estado = getString("estado") ?: "EXITOSA",
        auditada = getBoolean("auditada") ?: false,
        auditor = getString("auditor") ?: ""
    )
}