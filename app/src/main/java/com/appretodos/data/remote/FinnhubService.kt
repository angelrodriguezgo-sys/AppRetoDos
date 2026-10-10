package com.appretodos.data.remote



import com.appretodos.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object FinnhubService {

    /** Devuelve cuántas unidades de cada moneda equivalen a 1 [base]. */
    suspend fun obtenerTasas(base: String = "USD"): Map<String, Double> =
        withContext(Dispatchers.IO) {
            if (BuildConfig.FINNHUB_API_KEY.isBlank()) {
                throw IOException("Falta FINNHUB_API_KEY en local.properties")
            }
            val url = URL(
                "https://finnhub.io/api/v1/forex/rates?base=$base&token=${BuildConfig.FINNHUB_API_KEY}"
            )
            val conexion = url.openConnection() as HttpURLConnection
            try {
                conexion.connectTimeout = 10_000
                conexion.readTimeout = 10_000
                if (conexion.responseCode != 200) {
                    throw IOException("Finnhub respondió ${conexion.responseCode}")
                }
                val cuerpo = conexion.inputStream.bufferedReader().use { it.readText() }
                val quote = JSONObject(cuerpo).getJSONObject("quote")
                val resultado = mutableMapOf<String, Double>()
                for (codigo in quote.keys()) {
                    val valor = quote.optDouble(codigo, Double.NaN)
                    if (!valor.isNaN() && valor > 0.0) resultado[codigo] = valor
                }
                resultado
            } finally {
                conexion.disconnect()
            }
        }
}