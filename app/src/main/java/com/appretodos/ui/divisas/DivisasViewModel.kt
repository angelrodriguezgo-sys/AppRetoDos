package com.appretodos.ui.divisas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appretodos.data.AppContainer
import com.appretodos.data.model.Moneda
import com.appretodos.data.repository.EstadoTasas
import com.appretodos.data.repository.MonedasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CotizacionUi(val moneda: Moneda, val valorLocal: Double)

data class DivisasUiState(
    val monedaLocal: String = "",
    val destacadas: List<CotizacionUi> = emptyList(),
    val lista: List<CotizacionUi> = emptyList(),
    val estadoTasas: EstadoTasas = EstadoTasas(),
    val cargando: Boolean = false
)

class DivisasViewModel(
    private val repo: MonedasRepository = AppContainer.monedas
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _cargando = MutableStateFlow(false)

    val uiState: StateFlow<DivisasUiState> = combine(
        repo.monedaLocal,
        _busqueda,
        repo.monedasFlow,
        repo.estadoTasas,
        _cargando
    ) { local, texto, _, estado, cargando ->
        val aCotizacion = { m: Moneda -> CotizacionUi(m, repo.valorEnLocal(m.codigo, local)) }
        val todas = repo.monedas.map(aCotizacion)
        DivisasUiState(
            monedaLocal = local,
            destacadas = repo.destacadas.map { repo.obtener(it) }.map(aCotizacion),
            lista = if (texto.isBlank()) todas else todas.filter {
                it.moneda.codigo.contains(texto, ignoreCase = true) ||
                        it.moneda.nombre.contains(texto, ignoreCase = true)
            },
            estadoTasas = estado,
            cargando = cargando
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DivisasUiState())

    init {
        actualizar()
    }

    fun onBusqueda(texto: String) {
        _busqueda.value = texto
    }

    fun actualizar() {
        if (_cargando.value) return
        viewModelScope.launch {
            _cargando.value = true
            repo.actualizarTasas()
            _cargando.value = false
        }
    }
}