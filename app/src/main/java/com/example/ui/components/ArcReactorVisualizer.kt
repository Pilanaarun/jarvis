package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DeepBlueContainer
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.MutedGold
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcRotation")
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val reverseRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ReverseRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening || isSpeaking) 400 else 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "WavePhase"
    )

    val activeColor = when {
        isSpeaking -> MutedGold
        isListening -> IceBluePrimary
        else -> IceBluePrimary
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("arc_reactor_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = (this.size.minDimension / 2) * 0.85f * pulseScale

            // Outer Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        activeColor.copy(alpha = 0.45f),
                        activeColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.3f
                ),
                radius = radius * 1.3f,
                center = center
            )

            // Outer Tech Segment Ring (Rotating Clockwise)
            rotate(rotationAngle, pivot = center) {
                val segments = 12
                val sweepAngle = 20f
                for (i in 0 until segments) {
                    val startAngle = i * (360f / segments)
                    drawArc(
                        color = activeColor.copy(alpha = 0.7f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 4f)
                    )
                }
            }

            // Inner Reverse Segment Ring (Rotating Counter-Clockwise)
            val innerRadius = radius * 0.72f
            rotate(reverseRotationAngle, pivot = center) {
                val innerSegments = 8
                val sweepAngle = 30f
                for (i in 0 until innerSegments) {
                    val startAngle = i * (360f / innerSegments)
                    drawArc(
                        color = IceBluePrimary.copy(alpha = 0.9f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                        size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
                        style = Stroke(width = 6f)
                    )
                }
            }

            // Core Reactor Circle
            val coreRadius = radius * 0.45f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        activeColor,
                        activeColor.copy(alpha = 0.3f)
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // Animated Voice Wave Spectrum (When active or speaking)
            if (isListening || isSpeaking) {
                val waveCount = 16
                val wavePath = Path()
                for (i in 0 until waveCount) {
                    val angle = (i * 2 * Math.PI / waveCount).toFloat()
                    val amplitude = if (isSpeaking) 18f else 12f
                    val r = innerRadius + sin(angle * 4 + wavePhase) * amplitude
                    val x = center.x + r * cos(angle.toDouble()).toFloat()
                    val y = center.y + r * sin(angle.toDouble()).toFloat()

                    if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                }
                wavePath.close()

                drawPath(
                    path = wavePath,
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}
