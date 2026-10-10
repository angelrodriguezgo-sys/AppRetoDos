package com.appretodos.data.model

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
    val id: Int,
    val tipo: TipoOperacion,
    val codigoMoneda: String,
    val cantidad: Double,
    val valor: Double,
    val monedaValor: String,
    val fechaMillis: Long
)

data class Usuario(
    val nombre: String,
    val correo: String,
    val numeroCuenta: String,
    val idioma: String = "Español"
) {
    /** Lo que se muestra en el perfil: solo los últimos 4 dígitos. */
    val ultimosCuatro: String get() = numeroCuenta.takeLast(4)
}