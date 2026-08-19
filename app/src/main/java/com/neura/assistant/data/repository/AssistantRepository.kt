package com.neura.assistant.data.repository

import android.content.Context
import com.neura.assistant.data.api.GeminiService
import com.neura.assistant.data.api.models.GeminiContent
import com.neura.assistant.data.api.models.GeminiPart
import com.neura.assistant.data.api.models.GeminiRequest
import com.neura.assistant.data.api.models.GeminiSystemInstruction
import com.neura.assistant.data.api.models.GeminiTextPart
import com.neura.assistant.data.local.entities.MessageSender
import com.neura.assistant.data.local.entities.UiMessage
import com.neura.assistant.system.ActionResult
import com.neura.assistant.system.DeviceActionExecutor
import com.neura.assistant.system.DeviceToolsDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

sealed class AssistantState {
    object Idle : AssistantState()
    object Listening : AssistantState()
    data class Processing(val statusText: String = "Neura is thinking…") : AssistantState()
    data class Speaking(val text: String) : AssistantState()
    data class Error(val message: String) : AssistantState()
}

class AssistantRepository(
    private val context: Context,
    private val geminiService: GeminiService = GeminiService(),
    private val settingsRepository: SettingsRepository = SettingsRepository(context),
    private val deviceActionExecutor: DeviceActionExecutor = DeviceActionExecutor(context)
) {
    private val _messages = MutableStateFlow<List<UiMessage>>(
        listOf(
            UiMessage(
                sender = MessageSender.NEURA,
                text = "Hello! I am Neura, your next-generation AI assistant. How can I help you today?"
            )
        )
    )
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    private val _state = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val state: StateFlow<AssistantState> = _state.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val conversationHistory = mutableListOf<GeminiContent>()

    private val systemPrompt = """
        You are Neura, a powerful, intelligent, and elegant AI voice assistant on Android (just like Siri on iPhone, but for Android).
        You are lightning-fast, concise, helpful, and speak in a friendly, conversational tone suitable for voice synthesis.
        You have direct control over this Android device through your function calling tools:
        - Calling contacts & phone numbers (make_phone_call)
        - Sending SMS text messages (send_sms)
        - Opening any installed app on the device (open_app)
        - Setting alarms and countdown timers (set_alarm, set_timer)
        - Flashlight control (toggle_flashlight)
        - Volume adjustments and media controls (adjust_volume)
        - Live weather data and forecasts (get_weather)
        - Device battery level & system status (get_device_status)
        - Web searches & browser links (web_search, open_url)
        - Opening the camera & calendar events (open_camera, create_calendar_event)
        
        Keep your spoken responses concise and natural (1-2 sentences).
        When executing device actions, confirm the action smoothly.
    """.trimIndent()

    fun setAudioAmplitude(amplitude: Float) {
        _audioAmplitude.value = amplitude
    }

    fun setState(newState: AssistantState) {
        _state.value = newState
    }

    suspend fun processUserPrompt(promptText: String, onSpeechRequired: ((String) -> Unit)? = null) = withContext(Dispatchers.IO) {
        if (promptText.isBlank()) return@withContext

        // 1. Append user message to UI
        val userMsg = UiMessage(sender = MessageSender.USER, text = promptText)
        _messages.value = _messages.value + userMsg
        conversationHistory.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = promptText))
            )
        )

        _state.value = AssistantState.Processing("Neura is thinking…")

        try {
            val apiKey = settingsRepository.apiKeyFlow.first()
            val model = settingsRepository.modelNameFlow.first()

            val systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiTextPart(text = systemPrompt))
            )

            val geminiRequest = GeminiRequest(
                contents = conversationHistory.toList(),
                systemInstruction = systemInstruction,
                tools = listOf(DeviceToolsDefinition.geminiToolWrapper)
            )

            val responseResult = geminiService.generateContent(apiKey, geminiRequest, model)
            if (responseResult.isFailure) {
                val err = responseResult.exceptionOrNull()?.message ?: "Unknown error"
                _state.value = AssistantState.Error(err)
                _messages.value = _messages.value + UiMessage(
                    sender = MessageSender.SYSTEM,
                    text = "Error: $err"
                )
                return@withContext
            }

            val geminiResponse = responseResult.getOrThrow()
            val candidate = geminiResponse.candidates?.firstOrNull()
            val modelContent = candidate?.content

            val functionCalls = modelContent?.parts?.mapNotNull { it.functionCall } ?: emptyList()

            val finalReply: String

            if (functionCalls.isNotEmpty()) {
                val executedMessages = mutableListOf<String>()

                for (fc in functionCalls) {
                    val functionName = fc.name
                    val functionArgs = fc.args?.toString() ?: "{}"

                    _state.value = AssistantState.Processing("Executing $functionName…")
                    val toolResult = deviceActionExecutor.executeTool(functionName, functionArgs)

                    when (toolResult) {
                        is ActionResult.Success -> {
                            executedMessages.add(toolResult.message)
                            _messages.value = _messages.value + UiMessage(
                                sender = MessageSender.NEURA,
                                text = toolResult.message,
                                cardData = toolResult.cardData,
                                toolName = functionName
                            )
                        }
                        is ActionResult.Error -> {
                            executedMessages.add("Failed to execute $functionName: ${toolResult.errorMessage}")
                            _messages.value = _messages.value + UiMessage(
                                sender = MessageSender.SYSTEM,
                                text = "Failed to execute $functionName: ${toolResult.errorMessage}"
                            )
                        }
                    }
                }

                finalReply = executedMessages.joinToString(". ")
            } else {
                finalReply = modelContent?.parts?.mapNotNull { it.text }?.joinToString(" ")?.trim()
                    ?: "I'm here to help."

                _messages.value = _messages.value + UiMessage(
                    sender = MessageSender.NEURA,
                    text = finalReply
                )
            }

            conversationHistory.add(
                GeminiContent(
                    role = "model",
                    parts = listOf(GeminiPart(text = finalReply))
                )
            )

            _state.value = AssistantState.Speaking(finalReply)
            onSpeechRequired?.invoke(finalReply)

        } catch (e: Exception) {
            val err = e.message ?: "Failed to process request"
            _state.value = AssistantState.Error(err)
            _messages.value = _messages.value + UiMessage(
                sender = MessageSender.SYSTEM,
                text = "Error: $err"
            )
        }
    }

    fun clearHistory() {
        conversationHistory.clear()
        _messages.value = listOf(
            UiMessage(
                sender = MessageSender.NEURA,
                text = "Conversation cleared. How can I assist you now?"
            )
        )
        _state.value = AssistantState.Idle
    }
}
