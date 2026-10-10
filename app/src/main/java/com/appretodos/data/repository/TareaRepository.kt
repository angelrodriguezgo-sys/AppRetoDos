package com.appretodos.data.repository


import com.appretodos.data.model.Tarea
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TareaRepository {

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: Flow<List<Tarea>> = _tareas.asStateFlow()

    fun agregar(titulo: String) {
        _tareas.update { lista ->
            lista + Tarea(id = (lista.maxOfOrNull { it.id } ?: 0) + 1, titulo = titulo)
        }
    }

    fun alternar(id: Int) {
        _tareas.update { lista ->
            lista.map { if (it.id == id) it.copy(completada = !it.completada) else it }
        }
    }

    fun eliminar(id: Int) {
        _tareas.update { lista -> lista.filterNot { it.id == id } }
    }
}