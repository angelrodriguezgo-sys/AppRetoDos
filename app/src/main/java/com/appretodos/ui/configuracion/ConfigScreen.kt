package com.appretodos.ui.configuracion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appretodos.ui.components.Selector

@Composable
fun ConfigScreen(
    onVolver: () -> Unit,
    viewModel: ConfigViewModel = viewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Configuración",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(24.dp))

        Text("Moneda local", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Las cotizaciones se mostrarán en esta moneda.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Selector(
            textoBoton = s.monedaLocal,
            opciones = viewModel.monedas.map { it.codigo to "${it.codigo} - ${it.nombre}" },
            onSeleccion = viewModel::onMonedaLocal
        )

        Spacer(Modifier.height(24.dp))
        Text("Idioma de la app", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        listOf("Español", "English").forEach { idioma ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onIdioma(idioma) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = s.idioma == idioma, onClick = { viewModel.onIdioma(idioma) })
                Text(idioma)
            }
        }
    }
}