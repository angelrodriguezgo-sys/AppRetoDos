package com.appretodos.ui.components


import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun BotonFoto(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // rememberSaveable: si Android recrea la actividad mientras se usa la cámara, no se pierde la ruta
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val archivo = rutaPendiente?.let { File(it) }
        if (ok && archivo != null) {
            Toast.makeText(context, "Foto guardada", Toast.LENGTH_SHORT).show()
        } else {
            archivo?.delete() // cancelaron: no dejar un archivo vacío
        }
        rutaPendiente = null
    }

    SmallFloatingActionButton(
        modifier = modifier,
        onClick = {
            val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
            val archivo = File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
            rutaPendiente = archivo.absolutePath
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )
            try {
                tomarFoto.launch(uri)
            } catch (e: ActivityNotFoundException) {
                archivo.delete()
                rutaPendiente = null
                Toast.makeText(context, "No hay una app de cámara disponible", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Text("📷")
    }
}