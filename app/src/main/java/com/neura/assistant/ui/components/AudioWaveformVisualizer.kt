package com.neura.assistant.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neura.assistant.ui.theme.NeuraCyan
import com.neura.assistant.ui.theme.NeuraMagenta
import com.neura.assistant.ui.theme.NeuraPurple

@Composable
fun AudioWaveformVisualizer(
    isListening: Boolean,
    amplitude: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    barMaxHeight: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(barMaxHeight),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animDuration = 400 + (i * 45)
            val idleHeightFactor by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 0.45f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Bar_$i"
            )

            val dynamicHeightFraction = if (isListening) {
                val distanceToCenter = Math.abs(i - (barCount / 2f)) / (barCount / 2f)
                val shapeWeight = 1f - (distanceToCenter * 0.5f)
                (0.1f + (amplitude * shapeWeight * 0.9f)).coerceIn(0.08f, 1.0f)
            } else {
                idleHeightFactor * 0.4f
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(dynamicHeightFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NeuraCyan, NeuraPurple, NeuraMagenta)
                        )
                    )
            )
        }
    }
}
