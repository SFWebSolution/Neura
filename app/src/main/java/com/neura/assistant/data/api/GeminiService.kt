package com.neura.assistant.data.api

import com.neura.assistant.data.api.models.GeminiRequest
import com.neura.assistant.data.api.models.GeminiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiService(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(
        apiKey: String,
        request: GeminiRequest,
        modelName: String = "gemini-flash-latest"
    ): Result<GeminiResponse> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API key is missing."))
            }

            val targetModel = if (modelName.isBlank()) "gemini-flash-latest" else modelName
            val requestBody = json.encodeToString(request).toRequestBody(jsonMediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini Error (${response.code}): $responseBody"))
            }

            val geminiResponse = json.decodeFromString<GeminiResponse>(responseBody)
            if (geminiResponse.error != null) {
                return@withContext Result.failure(Exception("Gemini Error: ${geminiResponse.error.message}"))
            }

            Result.success(geminiResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
