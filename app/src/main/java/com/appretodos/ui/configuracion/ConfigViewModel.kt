package com.appretodos.ui.configuracion



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appretodos.data.AppContainer
import com.appretodos.data.model.Moneda
import com.appretodos.data.repository.MonedasRepository
import com.appretodos.data.repository.SesionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConfigUiState(
    val monedaLocal: String = "COP",
    val idioma: String = "Español"
)

class ConfigViewModel(
    private val repoMonedas: MonedasRepository = AppContainer.monedas,
    private val sesion: SesionRepository = AppContainer.sesion
) : ViewModel() {

    val monedas: List<Moneda> = repoMonedas.monedas

    val uiState: StateFlow<ConfigUiState> = combine(
        repoMonedas.monedaLocal,
        sesion.usuarioActual
    ) { local, usuario ->
        ConfigUiState(monedaLocal = local, idioma = usuario?.idioma ?: "Español")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConfigUiState())

    fun onMonedaLocal(codigo: String) {
        viewModelScope.launch { sesion.cambiarMonedaRecepcion(codigo) }
    }

    fun onIdioma(idioma: String) {
        viewModelScope.launch { sesion.cambiarIdioma(idioma) }
    }
}