package com.app.officegrid.tasks.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.ui.theme.*

@Composable
fun CircularProgressIndicator(
    progress: Float,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            drawArc(
                color = WarmBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
        }

        // Progress Arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
        }

        // Center Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = DeepCharcoal
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = StoneGray
            )
        }
    }
}

@Composable
fun LinearTaskProgress(
    completed: Int,
    total: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepCharcoal
                )
                Text(
                    text = "$completed / $total",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    color = StoneGray
                )
            }

            Spacer(Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    progress >= 0.8f -> ProfessionalSuccess
                    progress >= 0.5f -> ProfessionalWarning
                    else -> ProfessionalError
                },
                trackColor = WarmBorder,
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${(progress * 100).toInt()}% Complete",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = StoneGray
            )
        }
    }
}

@Composable
fun TaskMilestoneTracker(
    milestones: List<Milestone>,
    currentMilestone: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        milestones.forEachIndexed { index, milestone ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Milestone Indicator
                Surface(
                    modifier = Modifier.size(32.dp),
                    color = when {
                        index < currentMilestone -> ProfessionalSuccess
                        index == currentMilestone -> ProfessionalWarning
                        else -> WarmBorder
                    },
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (index <= currentMilestone) Color.White else StoneGray
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Milestone Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = milestone.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (index == currentMilestone) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (index <= currentMilestone) DeepCharcoal else StoneGray
                    )
                    if (milestone.description.isNotEmpty()) {
                        Text(
                            text = milestone.description,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = StoneGray
                        )
                    }
                }

                // Status Badge
                if (index < currentMilestone) {
                    Surface(
                        color = ProfessionalSuccess.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "✓",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfessionalSuccess
                        )
                    }
                }
            }

            // Connector Line
            if (index < milestones.size - 1) {
                Spacer(Modifier.height(8.dp))
                Canvas(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .width(2.dp)
                        .height(24.dp)
                ) {
                    drawLine(
                        color = if (index < currentMilestone) ProfessionalSuccess else WarmBorder,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 4.dp.toPx()
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

data class Milestone(
    val title: String,
    val description: String = ""
)

