package com.appretodos.data

import com.appretodos.data.repository.AuditoriaRepository
import com.appretodos.data.repository.BilleteraRepository
import com.appretodos.data.repository.MonedasRepository
import com.appretodos.data.repository.SesionRepository

/** Contenedor simple: una única instancia de cada repositorio para toda la app. */
object AppContainer {
    val monedas = MonedasRepository()
    val sesion = SesionRepository(monedas)
    val billetera = BilleteraRepository(monedas, sesion)
    val auditoria = AuditoriaRepository(sesion)
}