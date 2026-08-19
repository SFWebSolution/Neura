package com.neura.assistant.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class WeatherResult(
    val location: String,
    val temperatureCelsius: Double,
    val weatherDescription: String,
    val windSpeedKmh: Double,
    val humidityPercent: Int,
    val isDay: Boolean
)

class WeatherService(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    suspend fun fetchWeather(locationQuery: String): Result<WeatherResult> = withContext(Dispatchers.IO) {
        try {
            // 1. Geocode location query
            val encodedLoc = URLEncoder.encode(locationQuery.trim(), StandardCharsets.UTF_8.toString())
            val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedLoc&count=1&language=en&format=json"
            val geoRequest = Request.Builder().url(geoUrl).build()

            val geoResponse = httpClient.newCall(geoRequest).execute()
            val geoBody = geoResponse.body?.string() ?: return@withContext Result.failure(Exception("Empty geocoding response"))
            val geoJson = json.parseToJsonElement(geoBody).jsonObject

            val results = geoJson["results"]?.jsonArray
            if (results == null || results.isEmpty()) {
                return@withContext Result.failure(Exception("Location '$locationQuery' not found"))
            }

            val firstLoc = results[0].jsonObject
            val lat = firstLoc["latitude"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val lon = firstLoc["longitude"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val name = firstLoc["name"]?.jsonPrimitive?.content ?: locationQuery
            val country = firstLoc["country"]?.jsonPrimitive?.content ?: ""
            val fullLocationName = if (country.isNotBlank()) "$name, $country" else name

            // 2. Fetch current weather
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m"
            val weatherRequest = Request.Builder().url(weatherUrl).build()
            val weatherResponse = httpClient.newCall(weatherRequest).execute()
            val weatherBody = weatherResponse.body?.string() ?: return@withContext Result.failure(Exception("Empty weather response"))

            val weatherJson = json.parseToJsonElement(weatherBody).jsonObject
            val current = weatherJson["current"]?.jsonObject ?: return@withContext Result.failure(Exception("Missing current weather data"))

            val temp = current["temperature_2m"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val humidity = current["relative_humidity_2m"]?.jsonPrimitive?.content?.toDoubleOrNull()?.toInt() ?: 0
            val isDay = (current["is_day"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1) == 1
            val weatherCode = current["weather_code"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val windSpeed = current["wind_speed_10m"]?.jsonPrimitive?.doubleOrNull ?: 0.0

            val desc = mapWmoCodeToDescription(weatherCode)

            Result.success(
                WeatherResult(
                    location = fullLocationName,
                    temperatureCelsius = temp,
                    weatherDescription = desc,
                    windSpeedKmh = windSpeed,
                    humidityPercent = humidity,
                    isDay = isDay
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapWmoCodeToDescription(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snowfall"
            80, 81, 82 -> "Rain showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Partly cloudy"
        }
    }
}
