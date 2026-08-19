package com.neura.assistant.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neura.assistant.data.repository.AssistantState
import com.neura.assistant.ui.theme.NeuraBlue
import com.neura.assistant.ui.theme.NeuraCyan
import com.neura.assistant.ui.theme.NeuraMagenta
import com.neura.assistant.ui.theme.NeuraPink
import com.neura.assistant.ui.theme.NeuraPurple
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NeuraGlowSphere(
    state: AssistantState,
    audioAmplitude: Float,
    modifier: Modifier = Modifier,
    sphereSize: Dp = 180.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeuraSphereTransition")

    // Continuous rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    // Breathing pulse
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breathing"
    )

    // Smooth audio reactivity
    val animatedAmplitude by animateFloatAsState(
        targetValue = audioAmplitude.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 80),
        label = "Amplitude"
    )

    // Compute dynamic state factors
    val isListening = state is AssistantState.Listening
    val isSpeaking = state is AssistantState.Speaking
    val isProcessing = state is AssistantState.Processing

    val baseScale = when {
        isListening -> 1.0f + (animatedAmplitude * 0.45f)
        isSpeaking -> 1.05f + (animatedAmplitude * 0.35f)
        isProcessing -> breathingScale * 1.08f
        else -> breathingScale
    }

    val primaryColor = when {
        isListening -> NeuraCyan
        isSpeaking -> NeuraMagenta
        isProcessing -> NeuraPurple
        else -> NeuraBlue
    }

    val secondaryColor = when {
        isListening -> NeuraPurple
        isSpeaking -> NeuraPink
        isProcessing -> NeuraCyan
        else -> NeuraMagenta
    }

    Box(
        modifier = modifier
            .size(sphereSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2.6f) * baseScale

            // 1. Outer ambient glow ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f),
                        secondaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.6f
                ),
                radius = radius * 1.6f,
                center = center
            )

            // 2. Rotating orbital wave rings
            rotate(rotationAngle, pivot = center) {
                drawOrbitalRings(center, radius, primaryColor, secondaryColor)
            }

            rotate(-rotationAngle * 1.5f, pivot = center) {
                drawOrbitalRings(center, radius * 0.9f, secondaryColor, primaryColor)
            }

            // 3. Core glowing fluid sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        primaryColor.copy(alpha = 0.85f),
                        secondaryColor.copy(alpha = 0.75f),
                        NeuraPurple.copy(alpha = 0.4f)
                    ),
                    center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 4. Subtle inner highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.7f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                    radius = radius * 0.5f
                ),
                radius = radius * 0.5f,
                center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f)
            )
        }
    }
}

private fun DrawScope.drawOrbitalRings(
    center: Offset,
    radius: Float,
    color1: Color,
    color2: Color
) {
    val ringCount = 3
    for (i in 0 until ringCount) {
        val angleOffset = (i * Math.PI / ringCount).toFloat()
        val rx = radius * 1.15f
        val ry = radius * 0.75f

        drawOval(
            brush = Brush.sweepGradient(
                colors = listOf(
                    color1.copy(alpha = 0.6f),
                    color2.copy(alpha = 0.8f),
                    color1.copy(alpha = 0.1f),
                    color2.copy(alpha = 0.6f)
                ),
                center = center
            ),
            topLeft = Offset(center.x - rx, center.y - ry),
            size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
            style = Stroke(width = 3.5f)
        )
    }
}
