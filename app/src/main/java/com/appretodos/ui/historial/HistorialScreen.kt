package com.appretodos.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appretodos.data.model.TipoOperacion
import com.appretodos.data.model.Transaccion
import com.appretodos.util.aFecha
import com.appretodos.util.aHora
import com.appretodos.util.aTexto

@Composable
fun HistorialScreen(viewModel: HistorialViewModel = viewModel()) {
    val lista by viewModel.transacciones.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Historial",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))

        if (lista.isEmpty()) {
            Text(
                "Todavía no has realizado compras ni ventas.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(lista, key = { it.id }) { FilaTransaccion(it) }
            }
        }
    }
}

@Composable
private fun FilaTransaccion(t: Transaccion) {
    val esCompra = t.tipo == TipoOperacion.COMPRA
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
            Column(Modifier.weight(1f)) {
                Text(
                    "${t.tipo.etiqueta} · ${t.codigoMoneda}",
                    fontWeight = FontWeight.Bold,
                    color = if (esCompra) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary
                )
                Text("${t.cantidad.aTexto()} ${t.codigoMoneda}")
                Text(
                    "${t.fechaMillis.aFecha()} · ${t.fechaMillis.aHora()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(t.valor.aTexto(), fontWeight = FontWeight.Bold)
                Text(t.monedaValor, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}