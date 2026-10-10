package com.appretodos.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appretodos.data.AppContainer
import com.appretodos.data.model.Moneda
import com.appretodos.data.model.Usuario
import com.appretodos.data.repository.MonedasRepository
import com.appretodos.data.repository.SesionRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val sesion: SesionRepository = AppContainer.sesion,
    repoMonedas: MonedasRepository = AppContainer.monedas
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sesion.usuarioActual
    val monedas: List<Moneda> = repoMonedas.monedas

    fun onMonedaRecepcion(codigo: String) {
        viewModelScope.launch { sesion.cambiarMonedaRecepcion(codigo) }
    }

    fun cerrarSesion() = sesion.cerrarSesion()
}