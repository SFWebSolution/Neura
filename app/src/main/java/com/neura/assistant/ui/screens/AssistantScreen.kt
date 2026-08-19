package com.neura.assistant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neura.assistant.data.repository.AssistantRepository
import com.neura.assistant.data.repository.AssistantState
import com.neura.assistant.ui.components.AudioWaveformVisualizer
import com.neura.assistant.ui.components.ChatBubble
import com.neura.assistant.ui.components.NeuraGlowSphere
import com.neura.assistant.ui.theme.NeuraBlue
import com.neura.assistant.ui.theme.NeuraCardDark
import com.neura.assistant.ui.theme.NeuraCyan
import com.neura.assistant.ui.theme.NeuraDarkBg
import com.neura.assistant.ui.theme.NeuraMagenta
import com.neura.assistant.ui.theme.NeuraPurple
import com.neura.assistant.ui.theme.TextPrimary
import com.neura.assistant.ui.theme.TextSecondary
import com.neura.assistant.ui.theme.TextTertiary

@Composable
fun AssistantScreen(
    repository: AssistantRepository,
    isListening: Boolean,
    partialSpeechText: String,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onOpenSettings: () -> Unit,
    onSendTextMessage: (String) -> Unit
) {
    val messages by repository.messages.collectAsState()
    val state by repository.state.collectAsState()
    val audioAmplitude by repository.audioAmplitude.collectAsState()

    var isTextMode by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickSuggestions = listOf(
        "💡 Turn on flashlight",
        "🌦️ Weather in New York",
        "⏰ Set alarm for 7:00 AM",
        "⚡ Check battery level",
        "📱 Open YouTube",
        "⏳ Set a 5-minute timer"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeuraDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.neura.assistant.R.drawable.neura_logo),
                    contentDescription = "Neura Logo",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NEURA",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = NeuraCyan
                    )
                    Text(
                        text = "Next-Gen AI Assistant",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }

            Row {
                IconButton(onClick = { repository.clearHistory() }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Chat",
                        tint = TextSecondary
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Center / Conversation Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sphere Header inside list or top
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        NeuraGlowSphere(
                            state = state,
                            audioAmplitude = audioAmplitude,
                            sphereSize = 150.dp,
                            onClick = {
                                if (isListening) onStopListening() else onStartListening()
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status / Partial result display
                        val statusText = when {
                            isListening && partialSpeechText.isNotBlank() -> "\"$partialSpeechText\""
                            isListening -> "Neura is listening…"
                            state is AssistantState.Processing -> (state as AssistantState.Processing).statusText
                            state is AssistantState.Speaking -> "Neura is speaking…"
                            else -> "Tap sphere or mic to talk"
                        }

                        Text(
                            text = statusText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isListening) NeuraCyan else TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        if (isListening) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AudioWaveformVisualizer(
                                isListening = true,
                                amplitude = audioAmplitude,
                                barMaxHeight = 28.dp
                            )
                        }
                    }
                }

                // Messages
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Suggestion Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickSuggestions.forEach { suggestion ->
                val cleanText = suggestion.substringAfter(" ")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NeuraCardDark)
                        .clickable {
                            onSendTextMessage(cleanText)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        // Bottom Controls Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isTextMode) {
                // Text Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isTextMode = false }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Mode",
                            tint = NeuraCyan
                        )
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask Neura anything…", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeuraCyan,
                            unfocusedBorderColor = NeuraCardDark,
                            focusedContainerColor = NeuraCardDark,
                            unfocusedContainerColor = NeuraCardDark
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSendTextMessage(textInput.trim())
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeuraCyan, NeuraPurple)))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = NeuraDarkBg
                        )
                    }
                }
            } else {
                // Voice Control Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isTextMode = true }) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Keyboard",
                            tint = TextSecondary
                        )
                    }

                    // Central glowing Mic button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (isListening) listOf(NeuraMagenta, NeuraPurple) else listOf(NeuraCyan, NeuraBlue)
                                )
                            )
                            .clickable {
                                if (isListening) onStopListening() else onStartListening()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (state is AssistantState.Processing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = TextPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = NeuraDarkBg,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        }
    }
}
