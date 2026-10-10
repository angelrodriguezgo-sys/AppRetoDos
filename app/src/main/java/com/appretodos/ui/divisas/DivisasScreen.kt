package com.appretodos.ui.divisas



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appretodos.util.aTextoCambio

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import com.appretodos.util.aHora



@Composable
fun DivisasScreen(viewModel: DivisasViewModel = viewModel()) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val actualizado = s.estadoTasas.actualizadoMillis
    val textoFuente = s.estadoTasas.error
        ?: actualizado?.let { "Tasas en vivo (Finnhub) · ${it.aHora()}" }
        ?: "Tasas simuladas"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Mercado de divisas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Valor de 1 unidad en ${s.monedaLocal}",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                textoFuente,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = viewModel::actualizar, enabled = !s.cargando) {
                Text("Actualizar")
            }
        }
        if (s.cargando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))

        // 5 monedas destacadas
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(s.destacadas, key = { it.moneda.codigo }) { TarjetaDestacada(it, s.monedaLocal) }
        }

        Spacer(Modifier.height(16.dp))

        // Buscador
        OutlinedTextField(
            value = busqueda,
            onValueChange = viewModel::onBusqueda,
            label = { Text("Buscar moneda") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Lista completa
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (s.lista.isEmpty()) {
                item { Text("Sin resultados", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(s.lista, key = { it.moneda.codigo }) { FilaCotizacion(it, s.monedaLocal) }
        }
    }
}

@Composable
private fun TarjetaDestacada(c: CotizacionUi, local: String) {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                c.moneda.codigo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                c.moneda.nombre,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                c.valorLocal.aTextoCambio(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(local, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun FilaCotizacion(c: CotizacionUi, local: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    c.moneda.simbolo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(c.moneda.codigo, fontWeight = FontWeight.Bold)
                Text(
                    c.moneda.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(c.valorLocal.aTextoCambio(), fontWeight = FontWeight.Bold)
                Text(local, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}