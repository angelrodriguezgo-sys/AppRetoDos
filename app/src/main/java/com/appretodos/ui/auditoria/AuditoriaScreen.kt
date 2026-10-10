package com.appretodos.ui.auditoria



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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appretodos.data.model.Transaccion
import com.appretodos.util.aFecha
import com.appretodos.util.aHora
import com.appretodos.util.aTexto
import com.appretodos.util.aTextoCambio
import com.appretodos.data.model.Reglas
@Composable
fun AuditoriaScreen(viewModel: AuditoriaViewModel = viewModel()) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Auditoría",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Regla: ninguna operación puede superar ${Reglas.LIMITE_SOBREPRECIO_PCT} % del valor de la divisa.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Resumen("Total", s.total, Modifier.weight(1f))
            Resumen("Infracciones", s.infracciones, Modifier.weight(1f))
            Resumen("Pendientes", s.pendientes, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))

        TabRow(selectedTabIndex = s.filtro.ordinal) {
            FiltroAuditoria.entries.forEach { filtro ->
                Tab(
                    selected = s.filtro == filtro,
                    onClick = { viewModel.onFiltro(filtro) },
                    text = { Text(filtro.etiqueta) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        s.mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (s.lista.isEmpty()) {
            Text("No hay operaciones para mostrar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(s.lista, key = { it.id }) { t ->
                    FilaAuditoria(t, onAuditar = { viewModel.auditar(t.id) })
                }
            }
        }
    }
}

@Composable
private fun Resumen(titulo: String, valor: Int, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelSmall)
            Text(valor.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilaAuditoria(t: Transaccion, onAuditar: () -> Unit) {
    val signo = if (t.desviacionPct >= 0) "+" else ""
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${t.tipo.etiqueta} · ${t.codigoMoneda}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (t.dentroDelLimite) "DENTRO DEL LÍMITE" else "INFRACCIÓN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (t.dentroDelLimite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
            Text(
                "${t.nombreUsuario} · ${t.correoUsuario}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text("Cantidad: ${t.cantidad.aTexto()} ${t.codigoMoneda}")
            Text("Mercado: ${t.tasaMercado.aTextoCambio()} ${t.monedaValor}")
            Text("Aplicado: ${t.precioUnitario.aTextoCambio()} ${t.monedaValor}")
            Text(
                "Desviación: $signo${t.desviacionPct.aTexto()} % (límite ${Reglas.LIMITE_SOBREPRECIO_PCT} %)",
                fontWeight = FontWeight.Medium
            )
            Text("Total: ${t.valor.aTexto()} ${t.monedaValor}")
            Text(
                "${if (t.estado == "EXITOSA") "Exitosa ✓" else t.estado} · ${t.fechaMillis.aFecha()} ${t.fechaMillis.aHora()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            if (t.auditada) {
                Text(
                    "Auditada por ${t.auditor}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(onClick = onAuditar) { Text("Marcar auditada") }
            }
        }
    }
}