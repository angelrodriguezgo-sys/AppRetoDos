package com.appretodos.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/** Menú desplegable genérico. [opciones] es una lista de pares (id, etiqueta). */
@Composable
fun Selector(
    textoBoton: String,
    opciones: List<Pair<String, String>>,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(
            onClick = { expandido = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(textoBoton, modifier = Modifier.weight(1f))
            Text("▾")
        }
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { (id, etiqueta) ->
                DropdownMenuItem(
                    text = { Text(etiqueta) },
                    onClick = {
                        onSeleccion(id)
                        expandido = false
                    }
                )
            }
        }
    }
}