package com.appretodos.util

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

fun Context.vibrar(duracionMs: Long = 300) {
    val vibrador: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    if (vibrador == null || !vibrador.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val efecto = VibrationEffect.createOneShot(duracionMs, VibrationEffect.DEFAULT_AMPLITUDE)
        @Suppress("DEPRECATION")
        vibrador.vibrate(
            efecto,
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
    } else {
        @Suppress("DEPRECATION")
        vibrador.vibrate(duracionMs)
    }
}