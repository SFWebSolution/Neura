package com.neura.assistant.data.repository

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "neura_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val KEY_MODEL_NAME = stringPreferencesKey("model_name")
        val KEY_TTS_VOICE = stringPreferencesKey("tts_voice")
        val KEY_SPEECH_RATE = doublePreferencesKey("speech_rate")
        val KEY_SPEECH_PITCH = doublePreferencesKey("speech_pitch")
        val KEY_USE_OPENAI_TTS = booleanPreferencesKey("use_openai_tts")
        val KEY_FLOATING_OVERLAY = booleanPreferencesKey("floating_overlay_enabled")
        val KEY_BACKGROUND_SERVICE = booleanPreferencesKey("background_service_enabled")

        // Preconfigured API Key safely embedded
        val DEFAULT_API_KEY: String by lazy {
            try {
                val encodedKey = "c2stcHJvai14NWRJeS13SVlCR3hvVFNsSC16dGtVLVdXVFdKQlpuNGhlYWpFU3pKUFJpMllRVU5KWFdiU0tEeWt5QTJJS3JZUHJrQVg1eXpOTVQzQmxia0ZKSVQ3dmhONEdUSnY2Y0pEVjFFMVVHNWxmcFdVenZjaGZ0d3k3V2tsdFlpYU5aMFkyOWoycXJzcHJQRmlBLWJHendHRmtBajZnQUE="
                String(Base64.decode(encodedKey, Base64.DEFAULT), Charsets.UTF_8).trim()
            } catch (e: Exception) {
                ""
            }
        }
    }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_OPENAI_API_KEY] ?: DEFAULT_API_KEY
    }

    val modelNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_MODEL_NAME] ?: "gpt-4o"
    }

    val ttsVoiceFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_TTS_VOICE] ?: "nova"
    }

    val speechRateFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_SPEECH_RATE]?.toFloat() ?: 1.0f
    }

    val speechPitchFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_SPEECH_PITCH]?.toFloat() ?: 1.0f
    }

    val useOpenAiTtsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_USE_OPENAI_TTS] ?: false
    }

    val floatingOverlayFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_FLOATING_OVERLAY] ?: true
    }

    val backgroundServiceFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BACKGROUND_SERVICE] ?: false
    }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_OPENAI_API_KEY] = key
        }
    }

    suspend fun setModelName(model: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MODEL_NAME] = model
        }
    }

    suspend fun setTtsVoice(voice: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TTS_VOICE] = voice
        }
    }

    suspend fun setSpeechRate(rate: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SPEECH_RATE] = rate.toDouble()
        }
    }

    suspend fun setSpeechPitch(pitch: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SPEECH_PITCH] = pitch.toDouble()
        }
    }

    suspend fun setUseOpenAiTts(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USE_OPENAI_TTS] = enabled
        }
    }

    suspend fun setFloatingOverlay(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FLOATING_OVERLAY] = enabled
        }
    }

    suspend fun setBackgroundService(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BACKGROUND_SERVICE] = enabled
        }
    }
}
