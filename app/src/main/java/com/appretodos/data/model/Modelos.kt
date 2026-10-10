package com.appretodos.data.model

import kotlin.math.abs

data class Moneda(
    val codigo: String,
    val nombre: String,
    val simbolo: String,
    val tasaUsd: Double // cuántas unidades de esta moneda equivalen a 1 USD
)

enum class TipoOperacion(val etiqueta: String) {
    COMPRA("Compra"),
    VENTA("Venta")
}

data class Transaccion(
    val id: String,
    val uid: String = "",
    val tipo: TipoOperacion,
    val codigoMoneda: String,
    val cantidad: Double,
    val tasaMercado: Double,
    val precioUnitario: Double,
    val valor: Double,
    val monedaValor: String,
    val fechaMillis: Long,
    val nombreUsuario: String,
    val correoUsuario: String = "",
    val estado: String = "EXITOSA",
    val auditada: Boolean = false,
    val auditor: String = "",
    val auditadoMillis: Long = 0L
) {
    /** Desviación del precio aplicado frente al de mercado, en %. */
    val desviacionPct: Double
        get() = if (tasaMercado == 0.0) 0.0 else (precioUnitario / tasaMercado - 1.0) * 100.0

    val dentroDelLimite: Boolean
        get() = abs(desviacionPct) <= Reglas.LIMITE_SOBREPRECIO_PCT + 1e-6
}

data class Usuario(
    val uid: String = "",
    val nombre: String,
    val correo: String,
    val idioma: String = "Español",
    val numeroCuenta: String,
    val monedaRecepcion: String = "COP",
    val rol: Rol = Rol.USUARIO,
    val saldoUsd: Double = Reglas.SALDO_INICIAL_USD,
    val tenencias: Map<String, Double> = emptyMap()
) {
    /** Lo que se muestra en el perfil: solo los últimos 4 dígitos. */
    val ultimosCuatro: String get() = numeroCuenta.takeLast(4)
}
object Reglas {
    const val LIMITE_SOBREPRECIO_PCT = 2.5
    const val SALDO_INICIAL_USD = 1000.0
}