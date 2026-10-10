package com.appretodos.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthScreen(
    onAutenticado: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Cambio de Divisas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Compra y vende divisas fácilmente",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        TabRow(selectedTabIndex = if (s.modoRegistro) 1 else 0) {
            Tab(
                selected = !s.modoRegistro,
                onClick = { viewModel.cambiarModo(false) },
                text = { Text("Iniciar sesión") }
            )
            Tab(
                selected = s.modoRegistro,
                onClick = { viewModel.cambiarModo(true) },
                text = { Text("Registro") }
            )
        }
        Spacer(Modifier.height(16.dp))

        if (s.modoRegistro) {
            CampoTexto("Nombre completo", s.nombre, viewModel::onNombre)
        }
        CampoTexto("Correo electrónico", s.correo, viewModel::onCorreo, KeyboardType.Email)
        if (s.modoRegistro) {
            CampoTexto("Número de cuenta", s.cuenta, viewModel::onCuenta, KeyboardType.Number)
        }
        CampoTexto("Contraseña", s.password, viewModel::onPassword, KeyboardType.Password, oculto = true)

        s.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.enviar(onAutenticado) },
            enabled = !s.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                when {
                    s.cargando -> "Espera..."
                    s.modoRegistro -> "Crear cuenta"
                    else -> "Iniciar sesión"
                }
            )
        }
    }
}
@Composable
private fun CampoTexto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    tipo: KeyboardType = KeyboardType.Text,
    oculto: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = tipo),
        visualTransformation = if (oculto) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))
}