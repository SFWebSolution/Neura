package com.neura.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neura.assistant.system.ActionCardData
import com.neura.assistant.ui.theme.NeuraAmber
import com.neura.assistant.ui.theme.NeuraBlue
import com.neura.assistant.ui.theme.NeuraCardBorder
import com.neura.assistant.ui.theme.NeuraCardDark
import com.neura.assistant.ui.theme.NeuraCyan
import com.neura.assistant.ui.theme.NeuraGreen
import com.neura.assistant.ui.theme.NeuraMagenta
import com.neura.assistant.ui.theme.NeuraPink
import com.neura.assistant.ui.theme.NeuraPurple
import com.neura.assistant.ui.theme.TextPrimary
import com.neura.assistant.ui.theme.TextSecondary

@Composable
fun ActionCard(
    cardData: ActionCardData,
    modifier: Modifier = Modifier
) {
    when (cardData) {
        is ActionCardData.WeatherCard -> {
            CardContainer(
                title = "Weather Forecast",
                icon = Icons.Default.WbSunny,
                accentColor = NeuraAmber,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${cardData.temp.toInt()}°C",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = cardData.condition,
                            fontSize = 15.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = cardData.location,
                            fontSize = 13.sp,
                            color = NeuraCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Humidity: ${cardData.humidity}%",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wind: ${cardData.windSpeed.toInt()} km/h",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        is ActionCardData.CallCard -> {
            CardContainer(
                title = "Phone Call Initiated",
                icon = Icons.Default.Call,
                accentColor = NeuraGreen,
                modifier = modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NeuraGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = NeuraGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Calling",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = cardData.contactOrNumber,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        is ActionCardData.AlarmCard -> {
            CardContainer(
                title = "Alarm Scheduled",
                icon = Icons.Default.Alarm,
                accentColor = NeuraMagenta,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val timeStr = String.format("%02d:%02d", cardData.hour, cardData.minute)
                        Text(
                            text = timeStr,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = cardData.label,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeuraMagenta.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ON",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeuraMagenta
                        )
                    }
                }
            }
        }

        is ActionCardData.TimerCard -> {
            CardContainer(
                title = "Countdown Timer",
                icon = Icons.Default.HourglassTop,
                accentColor = NeuraPink,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val minutes = cardData.seconds / 60
                        val secs = cardData.seconds % 60
                        val timeStr = String.format("%02d:%02d", minutes, secs)
                        Text(
                            text = timeStr,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = cardData.label,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        is ActionCardData.FlashlightCard -> {
            CardContainer(
                title = "Flashlight",
                icon = Icons.Default.FlashlightOn,
                accentColor = NeuraCyan,
                modifier = modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashlightOn,
                        contentDescription = null,
                        tint = if (cardData.isOn) NeuraCyan else TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (cardData.isOn) "Torch is ON" else "Torch is OFF",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }

        is ActionCardData.AppLaunchCard -> {
            CardContainer(
                title = "Application Launcher",
                icon = Icons.Default.Launch,
                accentColor = NeuraBlue,
                modifier = modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = null,
                        tint = NeuraCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Opened ${cardData.appName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }

        is ActionCardData.TelemetryCard -> {
            CardContainer(
                title = "Device Telemetry",
                icon = Icons.Default.BatteryChargingFull,
                accentColor = NeuraGreen,
                modifier = modifier
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Battery: ${cardData.battery}% ${if (cardData.isCharging) "(Charging)" else ""}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeuraGreen
                        )
                        Text(
                            text = cardData.network,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cardData.time,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CardContainer(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NeuraCardDark)
            .border(1.dp, NeuraCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
