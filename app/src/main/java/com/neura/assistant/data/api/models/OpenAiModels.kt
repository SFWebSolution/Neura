package com.neura.assistant.data.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class OpenAiChatRequest(
    val model: String = "gpt-4o",
    val messages: List<OpenAiMessage>,
    @SerialName("tools")
    val tools: List<OpenAiTool>? = null,
    @SerialName("tool_choice")
    val toolChoice: String? = null,
    val temperature: Double = 0.7,
    @SerialName("max_tokens")
    val maxTokens: Int = 1000
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String? = null,
    val name: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<OpenAiToolCall>? = null,
    @SerialName("tool_call_id")
    val toolCallId: String? = null
)

@Serializable
data class OpenAiTool(
    val type: String = "function",
    val function: OpenAiFunction
)

@Serializable
data class OpenAiFunction(
    val name: String,
    val description: String,
    val parameters: JsonElement
)

@Serializable
data class OpenAiToolCall(
    val id: String,
    val type: String = "function",
    val function: OpenAiFunctionCall
)

@Serializable
data class OpenAiFunctionCall(
    val name: String,
    val arguments: String
)

@Serializable
data class OpenAiChatResponse(
    val id: String? = null,
    val choices: List<OpenAiChoice> = emptyList(),
    val usage: OpenAiUsage? = null,
    val error: OpenAiError? = null
)

@Serializable
data class OpenAiChoice(
    val index: Int = 0,
    val message: OpenAiMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class OpenAiUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0
)

@Serializable
data class OpenAiError(
    val message: String,
    val type: String? = null,
    val code: String? = null
)

@Serializable
data class OpenAiTtsRequest(
    val model: String = "tts-1",
    val input: String,
    val voice: String = "nova", // alloy, echo, fable, onyx, nova, shimmer
    @SerialName("response_format")
    val responseFormat: String = "mp3",
    val speed: Double = 1.0
)
