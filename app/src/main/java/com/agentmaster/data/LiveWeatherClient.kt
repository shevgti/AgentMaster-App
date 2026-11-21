package com.agentmaster.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class LiveWeatherSnapshot(
    val city: String,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val windKmh: Double,
    val precipitationMm: Double,
    val weatherCode: Int
)

class LiveWeatherClient(
    private val http: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(12, TimeUnit.SECONDS)
        .build()
) {
    suspend fun fetch(city: String): LiveWeatherSnapshot = withContext(Dispatchers.IO) {
        require(city.isNotBlank()) { "Укажите город" }
        val encoded = URLEncoder.encode(city.trim(), "UTF-8")
        val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=ru&format=json"
        val geo = getJson(geoUrl)
        val results = geo.optJSONArray("results")
            ?: throw IllegalStateException("Город не найден: $city")
        if (results.length() == 0) throw IllegalStateException("Город не найден: $city")
        val place = results.getJSONObject(0)
        val latitude = place.getDouble("latitude")
        val longitude = place.getDouble("longitude")
        val resolvedName = place.optString("name", city)
        val country = place.optString("country", "")
        val forecastUrl = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m&timezone=auto"
        val current = getJson(forecastUrl).getJSONObject("current")
        LiveWeatherSnapshot(
            city = listOf(resolvedName, country).filter { it.isNotBlank() }.joinToString(", "),
            temperatureC = current.getDouble("temperature_2m"),
            feelsLikeC = current.getDouble("apparent_temperature"),
            windKmh = current.getDouble("wind_speed_10m"),
            precipitationMm = current.getDouble("precipitation"),
            weatherCode = current.getInt("weather_code")
        )
    }

    private fun getJson(url: String): JSONObject {
        val request = Request.Builder().url(url).header("Accept", "application/json").build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Сервис погоды ответил: HTTP ${response.code}")
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) throw IllegalStateException("Пустой ответ сервиса погоды")
            return JSONObject(body)
        }
    }
}
