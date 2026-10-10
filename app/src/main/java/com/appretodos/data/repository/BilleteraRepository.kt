package com.appretodos.data.repository

import com.appretodos.data.model.TipoOperacion
import com.appretodos.data.model.Transaccion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BilleteraRepository(private val monedas: MonedasRepository) {

    // Saldo inicial de prueba: 1000 USD
    private val _saldoUsd = MutableStateFlow(1000.0)
    val saldoUsd: StateFlow<Double> = _saldoUsd.asStateFlow()

    private val _tenencias = MutableStateFlow<Map<String, Double>>(emptyMap())
    val tenencias: StateFlow<Map<String, Double>> = _tenencias.asStateFlow()

    private val _transacciones = MutableStateFlow<List<Transaccion>>(emptyList())
    val transacciones: StateFlow<List<Transaccion>> = _transacciones.asStateFlow()

    /** Devuelve un mensaje de error, o null si la compra se realizó. */
    fun comprar(codigo: String, cantidad: Double): String? {
        val costoUsd = cantidad / monedas.obtener(codigo).tasaUsd
        if (costoUsd > _saldoUsd.value) return "Saldo insuficiente"
        _saldoUsd.update { it - costoUsd }
        _tenencias.update { mapa -> mapa + (codigo to ((mapa[codigo] ?: 0.0) + cantidad)) }
        registrar(TipoOperacion.COMPRA, codigo, cantidad)
        return null
    }

    /** Devuelve un mensaje de error, o null si la venta se realizó. */
    fun vender(codigo: String, cantidad: Double): String? {
        val actual = _tenencias.value[codigo] ?: 0.0
        if (cantidad > actual + 1e-9) return "No tienes suficiente $codigo"
        _saldoUsd.update { it + cantidad / monedas.obtener(codigo).tasaUsd }
        _tenencias.update { mapa ->
            val resto = actual - cantidad
            if (resto < 1e-9) mapa - codigo else mapa + (codigo to resto)
        }
        registrar(TipoOperacion.VENTA, codigo, cantidad)
        return null
    }

    private fun registrar(tipo: TipoOperacion, codigo: String, cantidad: Double) {
        val local = monedas.monedaLocal.value
        val valor = cantidad * monedas.valorEnLocal(codigo, local)
        _transacciones.update { lista ->
            listOf(
                Transaccion(
                    id = lista.size + 1,
                    tipo = tipo,
                    codigoMoneda = codigo,
                    cantidad = cantidad,
                    valor = valor,
                    monedaValor = local,
                    fechaMillis = System.currentTimeMillis()
                )
            ) + lista
        }
    }

    fun reiniciar() {
        _saldoUsd.value = 1000.0
        _tenencias.value = emptyMap()
        _transacciones.value = emptyList()
    }
}