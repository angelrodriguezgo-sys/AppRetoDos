package com.appretodos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.appretodos.ui.tareas.TareasScreen
import com.appretodos.ui.theme.AppRetoDosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRetoDosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TareasScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}