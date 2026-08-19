package com.neura.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neura.assistant.data.local.entities.MessageSender
import com.neura.assistant.data.local.entities.UiMessage
import com.neura.assistant.ui.theme.NeuraBlue
import com.neura.assistant.ui.theme.NeuraCardBorder
import com.neura.assistant.ui.theme.NeuraCardDark
import com.neura.assistant.ui.theme.NeuraCyan
import com.neura.assistant.ui.theme.NeuraMagenta
import com.neura.assistant.ui.theme.NeuraPurple
import com.neura.assistant.ui.theme.TextPrimary
import com.neura.assistant.ui.theme.TextSecondary

@Composable
fun ChatBubble(
    message: UiMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == MessageSender.USER
    val isSystem = message.sender == MessageSender.SYSTEM

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (message.cardData != null) {
            ActionCard(
                cardData = message.cardData,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(if (isSystem) 0.95f else 0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(
                    when {
                        isUser -> Brush.linearGradient(listOf(NeuraBlue, NeuraPurple))
                        isSystem -> Brush.linearGradient(listOf(Color(0xFF2D1822), Color(0xFF181B2B)))
                        else -> Brush.linearGradient(listOf(NeuraCardDark, NeuraCardDark))
                    }
                )
                .border(
                    width = 1.dp,
                    color = when {
                        isUser -> NeuraCyan.copy(alpha = 0.3f)
                        isSystem -> NeuraMagenta.copy(alpha = 0.4f)
                        else -> NeuraCardBorder
                    },
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isSystem) NeuraMagenta else TextPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}
