package com.appretodos.ui.principal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appretodos.data.model.Rol
import com.appretodos.ui.auditoria.AuditoriaScreen
import com.appretodos.ui.billetera.BilleteraScreen
import com.appretodos.ui.divisas.DivisasScreen
import com.appretodos.ui.historial.HistorialScreen
import com.appretodos.ui.perfil.PerfilScreen
import com.appretodos.ui.perfil.PerfilViewModel

private enum class Destino { HOME, MONEDAS, HISTORIAL, AUDITORIA, PERFIL }

private data class Seccion(val titulo: String, val icono: ImageVector, val destino: Destino)

private val seccionesUsuario = listOf(
    Seccion("Home", Icons.Default.Home, Destino.HOME),
    Seccion("Monedas", Icons.Default.ShoppingCart, Destino.MONEDAS),
    Seccion("Historial", Icons.AutoMirrored.Filled.List, Destino.HISTORIAL),
    Seccion("Perfil", Icons.Default.Person, Destino.PERFIL)
)

private val seccionesAdmin = listOf(
    Seccion("Home", Icons.Default.Home, Destino.HOME),
    Seccion("Auditoría", Icons.Default.CheckCircle, Destino.AUDITORIA),
    Seccion("Perfil", Icons.Default.Person, Destino.PERFIL)
)

@Composable
fun MainScreen(
    onCerrarSesion: () -> Unit,
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val usuario by perfilViewModel.usuario.collectAsStateWithLifecycle()
    val secciones = if (usuario?.rol == Rol.ADMIN) seccionesAdmin else seccionesUsuario
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val destino = secciones.getOrNull(tab)?.destino ?: Destino.HOME

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
            when (destino) {
                Destino.HOME -> DivisasScreen()
                Destino.MONEDAS -> BilleteraScreen()
                Destino.HISTORIAL -> HistorialScreen()
                Destino.AUDITORIA -> AuditoriaScreen()
                Destino.PERFIL -> PerfilScreen(onCerrarSesion = onCerrarSesion)
            }
        }
    }
}