package com.neura.assistant.data.local.entities

import com.neura.assistant.system.ActionCardData
import kotlinx.serialization.Serializable

enum class MessageSender {
    USER,
    NEURA,
    SYSTEM
}

data class UiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isExecutingTool: Boolean = false,
    val toolName: String? = null,
    val cardData: ActionCardData? = null
)
