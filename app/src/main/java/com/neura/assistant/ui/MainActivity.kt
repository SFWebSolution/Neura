package com.neura.assistant.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.neura.assistant.data.repository.AssistantRepository
import com.neura.assistant.data.repository.SettingsRepository
import com.neura.assistant.service.NeuraForegroundService
import com.neura.assistant.ui.screens.AssistantScreen
import com.neura.assistant.ui.screens.SettingsScreen
import com.neura.assistant.ui.theme.NeuraDarkBg
import com.neura.assistant.ui.theme.NeuraTheme
import com.neura.assistant.voice.SpeechRecognizerManager
import com.neura.assistant.voice.TextToSpeechManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var assistantRepository: AssistantRepository
    private lateinit var ttsManager: TextToSpeechManager
    private var speechRecognizerManager: SpeechRecognizerManager? = null

    private var isListeningState = mutableStateOf(false)
    private var partialSpeechTextState = mutableStateOf("")
    private var isAsleep = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (recordAudioGranted) {
            startListening()
        } else {
            Toast.makeText(this, "Microphone permission is needed for voice assistance", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsRepository = SettingsRepository(this)
        assistantRepository = AssistantRepository(this, settingsRepository = settingsRepository)

        // Automatically activate persistent background service
        try {
            NeuraForegroundService.start(this)
        } catch (e: Exception) {}

        ttsManager = TextToSpeechManager(
            context = this,
            onSpeakingStarted = {},
            onSpeakingFinished = {
                assistantRepository.setState(com.neura.assistant.data.repository.AssistantState.Idle)
                // Resume listening automatically so conversation continues
                runOnUiThread {
                    checkAndStartListening()
                }
            }
        )

        initSpeechRecognizer()
        handleIntent(intent)

        setContent {
            NeuraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NeuraDarkBg
                ) {
                    var currentScreen by remember { mutableStateOf("assistant") }
                    val coroutineScope = rememberCoroutineScope()

                    if (currentScreen == "settings") {
                        SettingsScreen(
                            settingsRepository = settingsRepository,
                            onBack = { currentScreen = "assistant" }
                        )
                    } else {
                        AssistantScreen(
                            repository = assistantRepository,
                            isListening = isListeningState.value,
                            partialSpeechText = partialSpeechTextState.value,
                            onStartListening = {
                                isAsleep = false
                                checkAndStartListening()
                            },
                            onStopListening = {
                                isAsleep = true
                                stopListening()
                            },
                            onOpenSettings = { currentScreen = "settings" },
                            onSendTextMessage = { text ->
                                coroutineScope.launch {
                                    sendUserPrompt(text)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            NeuraForegroundService.ACTION_START_LISTENING,
            Intent.ACTION_ASSIST,
            "android.intent.action.VOICE_ASSIST" -> {
                isAsleep = false
                checkAndStartListening()
            }
        }
    }

    private fun initSpeechRecognizer() {
        speechRecognizerManager = SpeechRecognizerManager(
            context = this,
            onPartialResult = { partial ->
                partialSpeechTextState.value = partial
            },
            onFinalResult = { finalResult ->
                partialSpeechTextState.value = ""
                isListeningState.value = false
                lifecycleScope.launch {
                    handleRecognizedSpeech(finalResult)
                }
            },
            onRmsChanged = { rms ->
                assistantRepository.setAudioAmplitude(rms)
            },
            onErrorOccurred = { _ ->
                isListeningState.value = false
                partialSpeechTextState.value = ""
                // Auto restart background listening
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(1200)
                    checkAndStartListening()
                }
            }
        )
    }

    private fun checkAndStartListening() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            startListening()
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startListening() {
        ttsManager.stop()
        isListeningState.value = true
        partialSpeechTextState.value = ""
        assistantRepository.setState(com.neura.assistant.data.repository.AssistantState.Listening)
        speechRecognizerManager?.startListening()
    }

    private fun stopListening() {
        isListeningState.value = false
        speechRecognizerManager?.stopListening()
        assistantRepository.setState(com.neura.assistant.data.repository.AssistantState.Idle)
    }

    private suspend fun handleRecognizedSpeech(speech: String) {
        val raw = speech.trim()
        val lower = raw.lowercase()

        // 1. Check for wake words
        val wakeWords = listOf("neura wake up", "wake up neura", "hey neura", "ok neura", "hello neura", "neura", "wake up")
        var matchedWakeWord: String? = null
        for (w in wakeWords) {
            if (lower.startsWith(w)) {
                matchedWakeWord = w
                break
            }
        }

        if (matchedWakeWord != null) {
            isAsleep = false
            val remainingCommand = lower.removePrefix(matchedWakeWord).trim().removePrefix("and").trim()
            if (remainingCommand.isBlank()) {
                val wakeGreeting = "I'm awake and listening, what can I do for you?"
                val pitch = settingsRepository.speechPitchFlow.first()
                val rate = settingsRepository.speechRateFlow.first()
                runOnUiThread {
                    ttsManager.speak(wakeGreeting, pitch, rate)
                }
                return
            } else {
                sendUserPrompt(remainingCommand)
                return
            }
        }

        // If in sleep mode and no wake word was spoken, ignore and keep listening in background
        if (isAsleep) {
            checkAndStartListening()
            return
        }

        // Check for sleep command
        if (lower == "sleep" || lower == "go to sleep" || lower == "stop listening" || lower == "goodbye" || lower == "bye") {
            isAsleep = true
            stopListening()
            val sleepMsg = "Going to sleep. Say 'Neura wake up' or tap the sphere whenever you need me."
            val pitch = settingsRepository.speechPitchFlow.first()
            val rate = settingsRepository.speechRateFlow.first()
            runOnUiThread {
                ttsManager.speak(sleepMsg, pitch, rate)
            }
            return
        }

        // Normal active conversation prompt
        sendUserPrompt(raw)
    }

    private suspend fun sendUserPrompt(prompt: String) {
        val pitch = settingsRepository.speechPitchFlow.first()
        val rate = settingsRepository.speechRateFlow.first()
        val useOpenAiTts = settingsRepository.useOpenAiTtsFlow.first()
        val apiKey = settingsRepository.apiKeyFlow.first()
        val voice = settingsRepository.ttsVoiceFlow.first()

        assistantRepository.processUserPrompt(prompt) { spokenResponse ->
            if (useOpenAiTts && apiKey.isNotBlank()) {
                val ttsFile = File(cacheDir, "neura_response.mp3")
                lifecycleScope.launch {
                    val openAiService = com.neura.assistant.data.api.OpenAiService()
                    val res = openAiService.generateSpeech(apiKey, spokenResponse, ttsFile, voice)
                    if (res.isSuccess) {
                        runOnUiThread { ttsManager.playAudioFile(ttsFile) }
                    } else {
                        runOnUiThread { ttsManager.speak(spokenResponse, pitch, rate) }
                    }
                }
            } else {
                runOnUiThread {
                    ttsManager.speak(spokenResponse, pitch, rate)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizerManager?.stopListening()
        ttsManager.shutdown()
    }
}
