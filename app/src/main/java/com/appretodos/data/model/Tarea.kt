package com.appretodos.data.model

data class Tarea(
    val id: Int,
    val titulo: String,
    val completada: Boolean = false
)