package com.appretodos.ui.billetera



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appretodos.data.AppContainer
import com.appretodos.data.model.Moneda
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
    val error: String? = null
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

    fun confirmar() {
        val d = _dialogo.value ?: return
        val cantidad = d.cantidad.replace(',', '.').toDoubleOrNull()
        if (cantidad == null || cantidad <= 0.0) {
            _dialogo.update { it?.copy(error = "Escribe una cantidad válida") }
            return
        }
        val error = if (d.tipo == TipoOperacion.COMPRA) {
            repoBilletera.comprar(d.codigo, cantidad)
        } else {
            repoBilletera.vender(d.codigo, cantidad)
        }
        if (error == null) {
            _dialogo.value = null
        } else {
            _dialogo.update { it?.copy(error = error) }
        }
    }
}