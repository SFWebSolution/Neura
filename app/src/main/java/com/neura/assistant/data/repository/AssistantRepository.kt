package com.neura.assistant.data.repository

import android.content.Context
import com.neura.assistant.data.api.OpenAiService
import com.neura.assistant.data.api.models.OpenAiChatRequest
import com.neura.assistant.data.api.models.OpenAiMessage
import com.neura.assistant.data.local.entities.MessageSender
import com.neura.assistant.data.local.entities.UiMessage
import com.neura.assistant.system.ActionResult
import com.neura.assistant.system.DeviceActionExecutor
import com.neura.assistant.system.DeviceToolsDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private val openAiService: OpenAiService = OpenAiService(),
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

    private val openAiHistory = mutableListOf<OpenAiMessage>()

    private val systemPrompt = """
        You are Neura, a powerful, intelligent, and elegant AI voice assistant on Android.
        You are lightning-fast, witty, concise, helpful, and speak in a friendly, conversational tone suitable for voice synthesis.
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
        
        Keep your spoken responses concise and natural (1-3 sentences unless asked for an in-depth explanation).
        When executing device actions, confirm the action smoothly.
    """.trimIndent()

    init {
        openAiHistory.add(OpenAiMessage(role = "system", content = systemPrompt))
    }

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
        openAiHistory.add(OpenAiMessage(role = "user", content = promptText))

        _state.value = AssistantState.Processing("Neura is thinking…")

        try {
            val apiKey = settingsRepository.apiKeyFlow.first()
            val model = settingsRepository.modelNameFlow.first()

            var chatRequest = OpenAiChatRequest(
                model = model,
                messages = openAiHistory.toList(),
                tools = DeviceToolsDefinition.allTools,
                temperature = 0.7
            )

            var responseResult = openAiService.createChatCompletion(apiKey, chatRequest)
            if (responseResult.isFailure) {
                val err = responseResult.exceptionOrNull()?.message ?: "Unknown error"
                _state.value = AssistantState.Error(err)
                _messages.value = _messages.value + UiMessage(
                    sender = MessageSender.SYSTEM,
                    text = "Error: $err"
                )
                return@withContext
            }

            var chatResponse = responseResult.getOrThrow()
            var choice = chatResponse.choices.firstOrNull()
            var assistantMsg = choice?.message

            // Handle Tool Calling recursion loop
            while (assistantMsg != null && !assistantMsg.toolCalls.isNullOrEmpty()) {
                openAiHistory.add(assistantMsg)

                for (toolCall in assistantMsg.toolCalls) {
                    val functionName = toolCall.function.name
                    val functionArgs = toolCall.function.arguments

                    _state.value = AssistantState.Processing("Executing $functionName…")

                    // Show tool execution in UI
                    val toolResult = deviceActionExecutor.executeTool(functionName, functionArgs)

                    val toolResultContent: String
                    when (toolResult) {
                        is ActionResult.Success -> {
                            toolResultContent = toolResult.message
                            if (toolResult.cardData != null) {
                                _messages.value = _messages.value + UiMessage(
                                    sender = MessageSender.NEURA,
                                    text = toolResult.message,
                                    cardData = toolResult.cardData,
                                    toolName = functionName
                                )
                            }
                        }
                        is ActionResult.Error -> {
                            toolResultContent = "Error: ${toolResult.errorMessage}"
                            _messages.value = _messages.value + UiMessage(
                                sender = MessageSender.SYSTEM,
                                text = "Failed to execute $functionName: ${toolResult.errorMessage}"
                            )
                        }
                    }

                    openAiHistory.add(
                        OpenAiMessage(
                            role = "tool",
                            name = functionName,
                            content = toolResultContent,
                            toolCallId = toolCall.id
                        )
                    )
                }

                // Call OpenAI again with the tool responses to produce natural voice answer
                chatRequest = OpenAiChatRequest(
                    model = model,
                    messages = openAiHistory.toList(),
                    tools = DeviceToolsDefinition.allTools,
                    temperature = 0.7
                )
                responseResult = openAiService.createChatCompletion(apiKey, chatRequest)
                if (responseResult.isFailure) break

                chatResponse = responseResult.getOrThrow()
                choice = chatResponse.choices.firstOrNull()
                assistantMsg = choice?.message
            }

            val finalReply = assistantMsg?.content ?: "Done!"
            openAiHistory.add(OpenAiMessage(role = "assistant", content = finalReply))

            _messages.value = _messages.value + UiMessage(
                sender = MessageSender.NEURA,
                text = finalReply
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
        openAiHistory.clear()
        openAiHistory.add(OpenAiMessage(role = "system", content = systemPrompt))
        _messages.value = listOf(
            UiMessage(
                sender = MessageSender.NEURA,
                text = "Conversation cleared. How can I assist you now?"
            )
        )
        _state.value = AssistantState.Idle
    }
}
