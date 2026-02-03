package com.app.officegrid.tasks.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun TaskTimer(
    taskId: String,
    onTimeLogged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var startTime by remember { mutableLongStateOf(0L) }

    // Timer effect
    LaunchedEffect(isRunning) {
        if (isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            while (isRunning) {
                delay(100L) // Update every 100ms for smooth display
                elapsedTime = System.currentTimeMillis() - startTime
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isRunning) ProfessionalSuccess.copy(alpha = 0.05f) else AccentGray,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isRunning) ProfessionalSuccess else WarmBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TIME_TRACKER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = MutedSlate
                )

                if (isRunning) {
                    Surface(
                        color = ProfessionalSuccess,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "● ACTIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Time Display
            Text(
                text = formatTime(elapsedTime),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 48.sp
                ),
                color = if (isRunning) ProfessionalSuccess else DeepCharcoal
            )

            Spacer(Modifier.height(20.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start/Pause Button
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) ProfessionalWarning else ProfessionalSuccess
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isRunning) "PAUSE" else "START",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Stop Button
                if (elapsedTime > 0) {
                    Button(
                        onClick = {
                            if (elapsedTime > 0) {
                                onTimeLogged(elapsedTime)
                            }
                            isRunning = false
                            elapsedTime = 0L
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepCharcoal
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "STOP & LOG",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Time Breakdown
            if (elapsedTime > 0) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TimeUnit(label = "HOURS", value = (elapsedTime / 3600000).toString())
                    TimeUnit(label = "MINS", value = ((elapsedTime % 3600000) / 60000).toString())
                    TimeUnit(label = "SECS", value = ((elapsedTime % 60000) / 1000).toString())
                }
            }
        }
    }
}

@Composable
private fun TimeUnit(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
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

private fun formatTime(millis: Long): String {
    val hours = millis / 3600000
    val minutes = (millis % 3600000) / 60000
    val seconds = (millis % 60000) / 1000
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

