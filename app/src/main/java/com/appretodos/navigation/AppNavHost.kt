package com.appretodos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appretodos.ui.auth.AuthScreen
import com.appretodos.ui.bienvenida.WelcomeScreen
import com.appretodos.ui.configuracion.ConfigScreen
import com.appretodos.ui.principal.MainScreen
import androidx.compose.runtime.LaunchedEffect
import com.appretodos.data.AppContainer

object Rutas {
    const val AUTH = "auth"
    const val BIENVENIDA = "bienvenida"
    const val CONFIG = "configuracion"
    const val PRINCIPAL = "principal"
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    LaunchedEffect(Unit) {
        if (AppContainer.sesion.restaurarSesion()) {
            nav.navigate(Rutas.BIENVENIDA) {
                popUpTo(Rutas.AUTH) { inclusive = true }
            }
        }
    }

    NavHost(navController = nav, startDestination = Rutas.AUTH) {
        composable(Rutas.AUTH) {
            AuthScreen(onAutenticado = {
                nav.navigate(Rutas.BIENVENIDA) {
                    popUpTo(Rutas.AUTH) { inclusive = true }
                }
            })
        }
        composable(Rutas.BIENVENIDA) {
            WelcomeScreen(
                onIniciar = { nav.navigate(Rutas.PRINCIPAL) },
                onConfiguracion = { nav.navigate(Rutas.CONFIG) }
            )
        }
        composable(Rutas.CONFIG) {
            ConfigScreen(onVolver = { nav.popBackStack() })
        }
        composable(Rutas.PRINCIPAL) {
            MainScreen(onCerrarSesion = {
                nav.navigate(Rutas.AUTH) {
                    popUpTo(nav.graph.id) { inclusive = true }
                }
            })
        }
    }
}