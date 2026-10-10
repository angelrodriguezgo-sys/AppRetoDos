package com.appretodos.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Double.aTexto(decimales: Int = 2): String =
    String.format(Locale.getDefault(), "%,.${decimales}f", this)

fun Double.aTextoCambio(): String = aTexto(if (this < 1.0) 4 else 2)

fun Long.aFecha(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this))

fun Long.aHora(): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(this))