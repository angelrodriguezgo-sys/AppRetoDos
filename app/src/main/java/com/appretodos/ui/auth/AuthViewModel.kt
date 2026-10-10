package com.appretodos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appretodos.data.AppContainer
import com.appretodos.data.repository.SesionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val modoRegistro: Boolean = false,
    val nombre: String = "",
    val correo: String = "",
    val cuenta: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val sesion: SesionRepository = AppContainer.sesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun cambiarModo(registro: Boolean) =
        _uiState.update { it.copy(modoRegistro = registro, error = null) }

    fun onNombre(valor: String) = _uiState.update { it.copy(nombre = valor, error = null) }
    fun onCorreo(valor: String) = _uiState.update { it.copy(correo = valor, error = null) }
    fun onCuenta(valor: String) =
        _uiState.update { it.copy(cuenta = valor.filter { c -> c.isDigit() }.take(20), error = null) }
    fun onPassword(valor: String) = _uiState.update { it.copy(password = valor, error = null) }

    fun enviar(onExito: () -> Unit) {
        val s = _uiState.value
        if (s.cargando) return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            val error = if (s.modoRegistro) {
                sesion.registrar(s.nombre.trim(), s.correo.trim(), s.cuenta.trim(), s.password)
            } else {
                sesion.iniciarSesion(s.correo.trim(), s.password)
            }
            if (error == null) {
                _uiState.value = AuthUiState()
                onExito()
            } else {
                _uiState.update { it.copy(cargando = false, error = error) }
            }
        }
    }
}