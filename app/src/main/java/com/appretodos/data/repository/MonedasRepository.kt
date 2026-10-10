package com.appretodos.data.repository

import com.appretodos.data.model.Moneda
import com.appretodos.data.remote.FinnhubService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EstadoTasas(
    val origen: String = "Simuladas",
    val actualizadoMillis: Long? = null,
    val error: String? = null
)

class MonedasRepository {

    // Tasas iniciales (de respaldo). Finnhub las reemplaza al actualizar.
    private val iniciales: List<Moneda> = listOf(
        Moneda("USD", "Dólar estadounidense", "$", 1.0),
        Moneda("EUR", "Euro", "€", 0.92),
        Moneda("JPY", "Yen japonés", "¥", 150.0),
        Moneda("MXN", "Peso mexicano", "$", 18.0),
        Moneda("GBP", "Libra esterlina", "£", 0.78),
        Moneda("COP", "Peso colombiano", "$", 4000.0),
        Moneda("CAD", "Dólar canadiense", "$", 1.36),
        Moneda("CHF", "Franco suizo", "Fr", 0.88),
        Moneda("BRL", "Real brasileño", "R$", 5.0),
        Moneda("ARS", "Peso argentino", "$", 900.0),
        Moneda("CLP", "Peso chileno", "$", 950.0),
        Moneda("PEN", "Sol peruano", "S/", 3.75),
        Moneda("AUD", "Dólar australiano", "$", 1.5),
        Moneda("CNY", "Yuan chino", "¥", 7.2)
    )

    private val _monedas = MutableStateFlow(iniciales)
    val monedasFlow: StateFlow<List<Moneda>> = _monedas.asStateFlow()
    val monedas: List<Moneda> get() = _monedas.value

    val destacadas: List<String> = listOf("USD", "EUR", "JPY", "MXN", "GBP")

    private val _monedaLocal = MutableStateFlow("COP")
    val monedaLocal: StateFlow<String> = _monedaLocal.asStateFlow()

    private val _estadoTasas = MutableStateFlow(EstadoTasas())
    val estadoTasas: StateFlow<EstadoTasas> = _estadoTasas.asStateFlow()

    fun cambiarMonedaLocal(codigo: String) {
        _monedaLocal.value = codigo
    }

    fun obtener(codigo: String): Moneda = monedas.first { it.codigo == codigo }

    /** Cuánto vale 1 unidad de [codigo] expresada en la moneda [local]. */
    fun valorEnLocal(codigo: String, local: String): Double =
        obtener(local).tasaUsd / obtener(codigo).tasaUsd

    /** Pide las tasas a Finnhub. Si falla, conserva las últimas disponibles. */
    suspend fun actualizarTasas() {
        try {
            val tasas = FinnhubService.obtenerTasas("USD")
            _monedas.update { lista ->
                lista.map { m ->
                    if (m.codigo == "USD") m
                    else tasas[m.codigo]?.let { m.copy(tasaUsd = it) } ?: m
                }
            }
            _estadoTasas.value = EstadoTasas("Finnhub", System.currentTimeMillis(), null)
        } catch (e: Exception) {
            _estadoTasas.update {
                it.copy(error = "No se pudo actualizar con Finnhub (${e.message}). Se usan las últimas tasas disponibles.")
            }
        }
    }
}