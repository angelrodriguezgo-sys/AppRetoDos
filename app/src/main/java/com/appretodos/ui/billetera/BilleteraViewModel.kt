package com.appretodos.ui.billetera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appretodos.data.AppContainer
import com.appretodos.data.model.Moneda
import com.appretodos.data.model.Reglas
import com.appretodos.data.model.TipoOperacion
import com.appretodos.data.repository.BilleteraRepository
import com.appretodos.data.repository.MonedasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class TenenciaUi(val moneda: Moneda, val cantidad: Double, val valorLocal: Double)

data class BilleteraUiState(
    val monedaLocal: String = "",
    val saldoLocal: Double = 0.0,
    val tenencias: List<TenenciaUi> = emptyList()
)

data class DialogoUi(
    val tipo: TipoOperacion,
    val codigo: String,
    val cantidad: String = "",
    val sobreprecio: String = "",
    val error: String? = null,
    val procesando: Boolean = false
)

class BilleteraViewModel(
    private val repoMonedas: MonedasRepository = AppContainer.monedas,
    private val repoBilletera: BilleteraRepository = AppContainer.billetera
) : ViewModel() {

    val monedas: List<Moneda> = repoMonedas.monedas

    val uiState: StateFlow<BilleteraUiState> = combine(
        repoMonedas.monedaLocal,
        repoBilletera.saldoUsd,
        repoBilletera.tenencias,
        repoMonedas.monedasFlow
    ) { local, saldoUsd, tenencias, _ ->
        BilleteraUiState(
            monedaLocal = local,
            saldoLocal = saldoUsd * repoMonedas.obtener(local).tasaUsd,
            tenencias = tenencias.map { (codigo, cantidad) ->
                TenenciaUi(
                    moneda = repoMonedas.obtener(codigo),
                    cantidad = cantidad,
                    valorLocal = cantidad * repoMonedas.valorEnLocal(codigo, local)
                )
            }.sortedBy { it.moneda.codigo }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BilleteraUiState())

    private val _dialogo = MutableStateFlow<DialogoUi?>(null)
    val dialogo: StateFlow<DialogoUi?> = _dialogo.asStateFlow()
    private val _operacionExitosa = Channel<Unit>(Channel.BUFFERED)
    val operacionExitosa = _operacionExitosa.receiveAsFlow()

    /** Precio de mercado de 1 unidad de [codigo] en la moneda del usuario. */
    fun tasaMercado(codigo: String): Double =
        repoMonedas.valorEnLocal(codigo, repoMonedas.monedaLocal.value)

    fun abrirDialogo(tipo: TipoOperacion, codigo: String) {
        _dialogo.value = DialogoUi(tipo, codigo)
    }

    fun cerrarDialogo() {
        _dialogo.value = null
    }

    fun onCodigo(codigo: String) = _dialogo.update { it?.copy(codigo = codigo, error = null) }

    fun onCantidad(texto: String) {
        val limpio = texto.filter { it.isDigit() || it == '.' || it == ',' }
        _dialogo.update { it?.copy(cantidad = limpio, error = null) }
    }

    fun onSobreprecio(texto: String) {
        val limpio = texto.filter { it.isDigit() || it == '.' || it == ',' }
        _dialogo.update { it?.copy(sobreprecio = limpio, error = null) }
    }

    fun confirmar() {
        val d = _dialogo.value ?: return
        if (d.procesando) return

        val cantidad = d.cantidad.replace(',', '.').toDoubleOrNull()
        if (cantidad == null || cantidad <= 0.0) {
            _dialogo.update { it?.copy(error = "Escribe una cantidad válida") }
            return
        }
        val pct = if (d.tipo == TipoOperacion.VENTA) {
            d.sobreprecio.replace(',', '.').toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }
        if (pct < 0.0 || pct > Reglas.LIMITE_SOBREPRECIO_PCT) {
            _dialogo.update {
                it?.copy(error = "El sobreprecio debe estar entre 0 y ${Reglas.LIMITE_SOBREPRECIO_PCT} %")
            }
            return
        }

        viewModelScope.launch {
            _dialogo.update { it?.copy(procesando = true, error = null) }
            val error = if (d.tipo == TipoOperacion.COMPRA) {
                repoBilletera.comprar(d.codigo, cantidad)
            } else {
                repoBilletera.vender(d.codigo, cantidad, pct)
            }
            if (error == null) {
                _dialogo.value = null
                _operacionExitosa.trySend(Unit)
            } else {
                _dialogo.update { it?.copy(procesando = false, error = error) }
            }
        }
    }
}