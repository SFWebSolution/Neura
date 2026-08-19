package com.neura.assistant.data.api

import com.neura.assistant.data.api.models.OpenAiChatRequest
import com.neura.assistant.data.api.models.OpenAiChatResponse
import com.neura.assistant.data.api.models.OpenAiTtsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class OpenAiService(
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

    suspend fun createChatCompletion(
        apiKey: String,
        request: OpenAiChatRequest
    ): Result<OpenAiChatResponse> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("OpenAI API key is missing. Please configure it in Settings."))
            }

            val requestBody = json.encodeToString(request).toRequestBody(jsonMediaType)
            val httpRequest = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("OpenAI Error (${response.code}): $responseBody"))
            }

            val chatResponse = json.decodeFromString<OpenAiChatResponse>(responseBody)
            Result.success(chatResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateSpeech(
        apiKey: String,
        text: String,
        outputFile: File,
        voice: String = "nova",
        speed: Double = 1.0
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("OpenAI API key is missing."))
            }

            val ttsRequest = OpenAiTtsRequest(
                model = "tts-1",
                input = text,
                voice = voice,
                speed = speed
            )

            val requestBody = json.encodeToString(ttsRequest).toRequestBody(jsonMediaType)
            val httpRequest = Request.Builder()
                .url("https://api.openai.com/v1/audio/speech")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(httpRequest).execute()
            if (!response.isSuccessful) {
                val err = response.body?.string() ?: "TTS generation failed"
                return@withContext Result.failure(Exception("TTS Error: $err"))
            }

            response.body?.byteStream()?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
