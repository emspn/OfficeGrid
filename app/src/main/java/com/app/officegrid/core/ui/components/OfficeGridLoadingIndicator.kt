package com.app.officegrid.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.officegrid.ui.theme.ProfessionalSuccess
import kotlin.math.cos
import kotlin.math.sin

/**
 * 🧊 THE NEURAL CUBE LOADER
 * Custom Canvas-drawn 3D wireframe cube that rotates.
 * Represents the processing power of the OfficeGrid registry.
 */
@Composable
fun OfficeGridLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    color: Color = Color(0xFF2196F3) // Branded Neural Blue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cube_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = size.toPx()
            val center = Offset(s / 2, s / 2)
            val radius = s / 3
            val rad = Math.toRadians(angle.toDouble())

            // 3D to 2D Projection Math
            fun project(x: Float, y: Offset): Offset {
                val cosA = cos(rad).toFloat()
                val sinA = sin(rad).toFloat()
                // Rotation on Y axis
                val rx = x * cosA 
                return Offset(center.x + rx, center.y + y.y - center.y)
            }

            val points = listOf(
                Offset(-radius, center.y - radius), Offset(radius, center.y - radius),
                Offset(radius, center.y + radius), Offset(-radius, center.y + radius)
            )

            // Draw Wireframe
            val strokeWidth = 2.dp.toPx()
            
            // Back face
            drawCircle(color = color.copy(alpha = 0.2f), radius = 4f, center = project(-radius, points[0]))
            
            // Draw connecting paths (Neural Links)
            val path = Path().apply {
                val p1 = project(-radius, points[0])
                val p2 = project(radius, points[1])
                val p3 = project(radius, points[2])
                val p4 = project(-radius, points[3])
                
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                lineTo(p4.x, p4.y)
                close()
            }
            
            drawPath(path, color = color, style = Stroke(width = strokeWidth))
            
            // Core Pulse
            drawCircle(
                color = ProfessionalSuccess.copy(alpha = 0.3f),
                radius = radius / 2,
                center = center
            )
        }
    }
}
