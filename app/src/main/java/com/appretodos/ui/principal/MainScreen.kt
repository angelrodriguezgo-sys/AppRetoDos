package com.appretodos.ui.principal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.appretodos.ui.billetera.BilleteraScreen
import com.appretodos.ui.divisas.DivisasScreen
import com.appretodos.ui.historial.HistorialScreen
import com.appretodos.ui.perfil.PerfilScreen

private data class Seccion(val titulo: String, val icono: ImageVector)

private val secciones = listOf(
    Seccion("Home", Icons.Default.Home),
    Seccion("Monedas", Icons.Default.ShoppingCart),
    Seccion("Historial", Icons.AutoMirrored.Filled.List),
    Seccion("Perfil", Icons.Default.Person)
)

@Composable
fun MainScreen(onCerrarSesion: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                secciones.forEachIndexed { indice, seccion ->
                    NavigationBarItem(
                        selected = tab == indice,
                        onClick = { tab = indice },
                        icon = { Icon(seccion.icono, contentDescription = seccion.titulo) },
                        label = { Text(seccion.titulo) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> DivisasScreen()
                1 -> BilleteraScreen()
                2 -> HistorialScreen()
                else -> PerfilScreen(onCerrarSesion = onCerrarSesion)
            }
        }
    }
}