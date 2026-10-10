package com.appretodos.ui.perfil

import androidx.lifecycle.ViewModel
import com.appretodos.data.AppContainer
import com.appretodos.data.model.Usuario
import com.appretodos.data.repository.SesionRepository
import kotlinx.coroutines.flow.StateFlow

class PerfilViewModel(
    private val sesion: SesionRepository = AppContainer.sesion
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sesion.usuarioActual

    fun cerrarSesion() {
        sesion.cerrarSesion()
        AppContainer.billetera.reiniciar()
    }
}