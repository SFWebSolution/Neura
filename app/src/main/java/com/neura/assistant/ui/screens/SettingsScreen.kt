package com.neura.assistant.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neura.assistant.data.repository.SettingsRepository
import com.neura.assistant.service.NeuraForegroundService
import com.neura.assistant.service.NeuraOverlayService
import com.neura.assistant.ui.theme.NeuraCardDark
import com.neura.assistant.ui.theme.NeuraCyan
import com.neura.assistant.ui.theme.NeuraDarkBg
import com.neura.assistant.ui.theme.NeuraGreen
import com.neura.assistant.ui.theme.NeuraMagenta
import com.neura.assistant.ui.theme.NeuraPurple
import com.neura.assistant.ui.theme.TextPrimary
import com.neura.assistant.ui.theme.TextSecondary
import com.neura.assistant.ui.theme.TextTertiary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentModel by settingsRepository.modelNameFlow.collectAsState(initial = "gpt-4o")
    val speechRate by settingsRepository.speechRateFlow.collectAsState(initial = 1.0f)
    val speechPitch by settingsRepository.speechPitchFlow.collectAsState(initial = 1.0f)
    val isOverlayEnabled by settingsRepository.floatingOverlayFlow.collectAsState(initial = true)
    val isBgServiceEnabled by settingsRepository.backgroundServiceFlow.collectAsState(initial = true)
    val useOpenAiTts by settingsRepository.useOpenAiTtsFlow.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeuraDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Neura Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Section: AI Intelligence & Engine Status
        SettingsSection(title = "AI Brain & Intelligence Engine", icon = Icons.Default.Key) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeuraDarkBg)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NeuraGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "OpenAI GPT-4o Engine Active",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pre-configured & authenticated securely",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Model: $currentModel",
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Button(
                    onClick = {
                        val nextModel = if (currentModel == "gpt-4o") "gpt-4o-mini" else "gpt-4o"
                        coroutineScope.launch { settingsRepository.setModelName(nextModel) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeuraPurple)
                ) {
                    Text("Switch Model", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Voice Synthesis
        SettingsSection(title = "Voice & Speech Synthesis", icon = Icons.Default.RecordVoiceOver) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Use OpenAI High-Quality Audio TTS", color = TextPrimary, fontSize = 14.sp)
                    Text("Natural expressive neural voices", color = TextSecondary, fontSize = 12.sp)
                }
                Switch(
                    checked = useOpenAiTts,
                    onCheckedChange = {
                        coroutineScope.launch { settingsRepository.setUseOpenAiTts(it) }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = NeuraCyan, checkedTrackColor = NeuraPurple)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Speech Rate (${String.format("%.1f", speechRate)}x)", color = TextSecondary, fontSize = 13.sp)
            Slider(
                value = speechRate,
                onValueChange = {
                    coroutineScope.launch { settingsRepository.setSpeechRate(it) }
                },
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(thumbColor = NeuraCyan, activeTrackColor = NeuraCyan)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Speech Pitch (${String.format("%.1f", speechPitch)}x)", color = TextSecondary, fontSize = 13.sp)
            Slider(
                value = speechPitch,
                onValueChange = {
                    coroutineScope.launch { settingsRepository.setSpeechPitch(it) }
                },
                valueRange = 0.5f..1.5f,
                colors = SliderDefaults.colors(thumbColor = NeuraMagenta, activeTrackColor = NeuraMagenta)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Background & System Invocations
        SettingsSection(title = "Continuous Background Listening", icon = Icons.Default.Layers) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Persistent Background Service", color = TextPrimary, fontSize = 14.sp)
                    Text("Keeps Neura ready anywhere until asked to sleep", color = TextSecondary, fontSize = 12.sp)
                }
                Switch(
                    checked = isBgServiceEnabled,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            settingsRepository.setBackgroundService(enabled)
                            if (enabled) {
                                NeuraForegroundService.start(context)
                            } else {
                                NeuraForegroundService.stop(context)
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = NeuraCyan, checkedTrackColor = NeuraPurple)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Floating Glowing Orb Bubble", color = TextPrimary, fontSize = 14.sp)
                    Text("Floats over apps for instant access", color = TextSecondary, fontSize = 12.sp)
                }
                Switch(
                    checked = isOverlayEnabled,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            settingsRepository.setFloatingOverlay(enabled)
                            if (enabled) {
                                if (Settings.canDrawOverlays(context)) {
                                    NeuraOverlayService.start(context)
                                } else {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                    context.startActivity(intent)
                                }
                            } else {
                                NeuraOverlayService.stop(context)
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = NeuraCyan, checkedTrackColor = NeuraPurple)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: System Default Assistant Integration
        SettingsSection(title = "Default Digital Assistant", icon = Icons.Default.Assistant) {
            Text(
                text = "Set Neura as your Android device's default assistant. Once set, holding your phone's power button or swiping from bottom corners activates Neura instantly.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (ex: Exception) {}
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeuraCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configure Default Assistant", color = NeuraDarkBg, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NeuraCardDark)
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeuraCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
