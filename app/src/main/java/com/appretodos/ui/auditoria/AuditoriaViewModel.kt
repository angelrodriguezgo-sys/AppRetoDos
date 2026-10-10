package com.appretodos.ui.auditoria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appretodos.data.AppContainer
import com.appretodos.data.model.Transaccion
import com.appretodos.data.repository.AuditoriaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FiltroAuditoria(val etiqueta: String) {
    TODAS("Todas"),
    INFRACCIONES("Infracciones"),
    PENDIENTES("Pendientes")
}

data class AuditoriaUiState(
    val filtro: FiltroAuditoria = FiltroAuditoria.TODAS,
    val lista: List<Transaccion> = emptyList(),
    val total: Int = 0,
    val infracciones: Int = 0,
    val pendientes: Int = 0,
    val mensaje: String? = null
)

class AuditoriaViewModel(
    private val repo: AuditoriaRepository = AppContainer.auditoria
) : ViewModel() {

    private val _filtro = MutableStateFlow(FiltroAuditoria.TODAS)
    private val _mensaje = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AuditoriaUiState> = combine(
        repo.transacciones,
        _filtro,
        _mensaje
    ) { todas, filtro, mensaje ->
        AuditoriaUiState(
            filtro = filtro,
            lista = when (filtro) {
                FiltroAuditoria.TODAS -> todas
                FiltroAuditoria.INFRACCIONES -> todas.filter { !it.dentroDelLimite }
                FiltroAuditoria.PENDIENTES -> todas.filter { !it.auditada }
            },
            total = todas.size,
            infracciones = todas.count { !it.dentroDelLimite },
            pendientes = todas.count { !it.auditada },
            mensaje = mensaje
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuditoriaUiState())

    fun onFiltro(filtro: FiltroAuditoria) {
        _filtro.value = filtro
    }

    fun auditar(id: String) {
        viewModelScope.launch { _mensaje.value = repo.marcarAuditada(id) }
    }
}