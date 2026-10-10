package com.appretodos.ui.bienvenida



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appretodos.ui.perfil.PerfilViewModel

@Composable
fun WelcomeScreen(
    onIniciar: () -> Unit,
    onConfiguracion: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val usuario by viewModel.usuario.collectAsStateWithLifecycle()
    val primerNombre = (usuario?.nombre ?: "").substringBefore(" ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "\$",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            if (primerNombre.isBlank()) "¡Bienvenido!" else "¡Bienvenido, $primerNombre!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Consulta el valor de las divisas, compra y vende monedas y revisa tu historial.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onIniciar,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Iniciar")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onConfiguracion,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Configuración")
        }
    }
}