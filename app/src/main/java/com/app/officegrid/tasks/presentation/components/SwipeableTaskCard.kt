package com.app.officegrid.tasks.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun SwipeableTaskCard(
    task: Task,
    onClick: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isRevealed by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val maxSwipe = with(density) { 160.dp.toPx() }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Background actions (revealed when swiped)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.White),
            horizontalArrangement = Arrangement.End
        ) {
            // Next Status Action
            Surface(
                onClick = {
                    val nextStatus = when (task.status) {
                        TaskStatus.TODO -> TaskStatus.IN_PROGRESS
                        TaskStatus.IN_PROGRESS -> TaskStatus.DONE
                        TaskStatus.DONE -> TaskStatus.TODO
                    }
                    onStatusChange(nextStatus)
                    offsetX = 0f
                },
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight(),
                color = ProfessionalWarning,
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Update Status",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "NEXT",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White
                    )
                }
            }

            // Delete Action
            Surface(
                onClick = {
                    onDelete()
                    offsetX = 0f
                },
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight(),
                color = ProfessionalError,
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "DELETE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White
                    )
                }
            }
        }

        // Task Card (swipeable)
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -maxSwipe / 2) {
                                offsetX = -maxSwipe
                                isRevealed = true
                            } else {
                                offsetX = 0f
                                isRevealed = false
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-maxSwipe, 0f)
                        }
                    )
                },
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
        ) {
            TaskCardContent(task = task)
        }
    }
}

@Composable
private fun TaskCardContent(task: Task) {
    val formattedDate = remember(task.dueDate) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.dueDate))
    }

    val accentColor = when (task.priority) {
        TaskPriority.HIGH -> ProfessionalError
        TaskPriority.MEDIUM -> ProfessionalWarning
        TaskPriority.LOW -> DeepCharcoal
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Priority Indicator
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .background(accentColor, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Status and Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.status.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    ),
                    color = when(task.status) {
                        TaskStatus.DONE -> ProfessionalSuccess
                        TaskStatus.IN_PROGRESS -> ProfessionalWarning
                        TaskStatus.TODO -> StoneGray
                    }
                )

                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = task.priority.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = Gray900,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Description
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Due Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Gray500
                )
                Text(
                    text = "DUE: $formattedDate",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = Gray500
                )
            }
        }
    }
}
