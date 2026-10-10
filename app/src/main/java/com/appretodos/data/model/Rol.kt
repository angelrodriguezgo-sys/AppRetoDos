package com.appretodos.data.model

enum class Rol {
    USUARIO,
    ADMIN;

    companion object {
        /** Convierte el texto guardado en Firestore en un Rol. Si es nulo o desconocido, devuelve USUARIO. */
        fun desde(valor: String?): Rol =
            values().firstOrNull { it.name.equals(valor?.trim(), ignoreCase = true) } ?: USUARIO
    }
}