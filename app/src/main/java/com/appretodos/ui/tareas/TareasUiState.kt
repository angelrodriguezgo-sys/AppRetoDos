package com.appretodos.ui.tareas

import com.appretodos.data.model.Tarea

data class TareasUiState(
    val tareas: List<Tarea> = emptyList(),
    val textoNuevo: String = ""
)