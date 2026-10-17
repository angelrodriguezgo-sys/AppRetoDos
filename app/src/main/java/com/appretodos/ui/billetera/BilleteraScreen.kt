package com.appretodos.ui.billetera

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appretodos.data.model.Moneda
import com.appretodos.data.model.TipoOperacion
import com.appretodos.ui.components.Selector
import com.appretodos.util.aTexto
import com.appretodos.util.aTextoCambio
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.appretodos.util.vibrar

@Composable
fun BilleteraScreen(viewModel: BilleteraViewModel = viewModel()) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogo by viewModel.dialogo.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        context.vibrar()   // PRUEBA TEMPORAL: debe vibrar al abrir la pestaña Monedas
        viewModel.operacionExitosa.collect { context.vibrar() }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Mis monedas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Saldo disponible", color = MaterialTheme.colorScheme.onPrimary)
                Text(
                    "${s.saldoLocal.aTexto()} ${s.monedaLocal}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { viewModel.abrirDialogo(TipoOperacion.COMPRA, "USD") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comprar divisa")
        }
        Spacer(Modifier.height(12.dp))

        if (s.tenencias.isEmpty()) {
            Text(
                "Aún no tienes monedas. Pulsa «Comprar divisa» para empezar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(s.tenencias, key = { it.moneda.codigo }) { t ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${t.moneda.codigo} · ${t.moneda.nombre}", fontWeight = FontWeight.Bold)
                            Text("Tienes: ${t.cantidad.aTexto()} ${t.moneda.codigo}")
                            Text(
                                "≈ ${t.valorLocal.aTexto()} ${s.monedaLocal}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.abrirDialogo(TipoOperacion.COMPRA, t.moneda.codigo) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Comprar") }
                                OutlinedButton(
                                    onClick = { viewModel.abrirDialogo(TipoOperacion.VENTA, t.moneda.codigo) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Vender") }
                            }
                        }
                    }
                }
            }
        }
    }

    dialogo?.let { d ->
        DialogoOperacion(
            d = d,
            monedas = viewModel.monedas,
            local = s.monedaLocal,
            tasaMercado = viewModel.tasaMercado(d.codigo),
            onCodigo = viewModel::onCodigo,
            onCantidad = viewModel::onCantidad,
            onSobreprecio = viewModel::onSobreprecio,
            onConfirmar = viewModel::confirmar,
            onCerrar = viewModel::cerrarDialogo
        )
    }
}

@Composable
private fun DialogoOperacion(
    d: DialogoUi,
    monedas: List<Moneda>,
    local: String,
    tasaMercado: Double,
    onCodigo: (String) -> Unit,
    onCantidad: (String) -> Unit,
    onSobreprecio: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    val esVenta = d.tipo == TipoOperacion.VENTA
    val cantidad = d.cantidad.replace(',', '.').toDoubleOrNull() ?: 0.0
    val pct = if (esVenta) d.sobreprecio.replace(',', '.').toDoubleOrNull() ?: 0.0 else 0.0
    val precio = tasaMercado * (1.0 + pct / 100.0)

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (esVenta) "Vender divisa" else "Comprar divisa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Selector(
                    textoBoton = d.codigo,
                    opciones = monedas.map { it.codigo to "${it.codigo} - ${it.nombre}" },
                    onSeleccion = onCodigo
                )
                OutlinedTextField(
                    value = d.cantidad,
                    onValueChange = onCantidad,
                    label = { Text("Cantidad de ${d.codigo}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                if (esVenta) {
                    OutlinedTextField(
                        value = d.sobreprecio,
                        onValueChange = onSobreprecio,
                        label = { Text("Sobreprecio % (0 a 2.5)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
                Text(
                    "Precio de mercado: ${tasaMercado.aTextoCambio()} $local",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (esVenta) {
                    Text(
                        "Tu precio: ${precio.aTextoCambio()} $local",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${if (esVenta) "Recibes" else "Pagas"}: ${(cantidad * precio).aTexto()} $local",
                    fontWeight = FontWeight.Bold
                )
                d.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = onConfirmar, enabled = !d.procesando) {
                Text(if (d.procesando) "Procesando..." else "Confirmar")
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}