package com.appretodos.ui.tareas



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appretodos.data.repository.TareaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TareasViewModel(
    private val repository: TareaRepository = TareaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TareasUiState())
    val uiState: StateFlow<TareasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.tareas.collect { lista ->
                _uiState.update { it.copy(tareas = lista) }
            }
        }
    }

    fun onTextoCambiado(texto: String) {
        _uiState.update { it.copy(textoNuevo = texto) }
    }

    fun onAgregar() {
        val texto = _uiState.value.textoNuevo.trim()
        if (texto.isNotEmpty()) {
            repository.agregar(texto)
            _uiState.update { it.copy(textoNuevo = "") }
        }
    }

    fun onAlternar(id: Int) = repository.alternar(id)

    fun onEliminar(id: Int) = repository.eliminar(id)
}