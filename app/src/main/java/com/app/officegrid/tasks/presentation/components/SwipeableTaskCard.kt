package com.app.officegrid.tasks.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableTaskCard(
    task: Task,
    onClick: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Approve Confirmation Dialog
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ProfessionalSuccess,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Approve Task Completion?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        "Do you want to approve this task as completed?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Task: ${task.title}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStatusChange(TaskStatus.DONE)
                        showApproveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalSuccess)
                ) {
                    Text("YES, APPROVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Reject Confirmation Dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = ProfessionalError,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Reject Task Completion?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        "Send this task back to the employee for more work?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Task: ${task.title}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStatusChange(TaskStatus.IN_PROGRESS)
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalError)
                ) {
                    Text("YES, REJECT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = ProfessionalError,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Delete Task?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to permanently delete this task?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Task: ${task.title}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ProfessionalError
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StoneGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalError)
                ) {
                    Text("YES, DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Options Menu (shown on long press)
    if (showOptionsMenu) {
        AlertDialog(
            onDismissRequest = { showOptionsMenu = false },
            title = {
                Text(
                    "Task Actions",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Edit Option (if available)
                    if (onEdit != null) {
                        Surface(
                            onClick = {
                                onEdit()
                                showOptionsMenu = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = DeepCharcoal.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, null, tint = DeepCharcoal)
                                Text("Edit Task", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    // Change Status Option
                    Surface(
                        onClick = {
                            val nextStatus = when (task.status) {
                                TaskStatus.TODO -> TaskStatus.IN_PROGRESS
                                TaskStatus.IN_PROGRESS -> TaskStatus.PENDING_COMPLETION
                                TaskStatus.PENDING_COMPLETION -> TaskStatus.DONE
                                TaskStatus.DONE -> TaskStatus.TODO
                            }
                            onStatusChange(nextStatus)
                            showOptionsMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = ProfessionalWarning.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = ProfessionalWarning)
                            Text("Change Status", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    // Delete Option
                    Surface(
                        onClick = {
                            showOptionsMenu = false
                            showDeleteDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = ProfessionalError.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, null, tint = ProfessionalError)
                            Text("Delete Task", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOptionsMenu = false }) {
                    Text("CLOSE")
                }
            }
        )
    }

    // Task Card with Long Press
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showOptionsMenu = true }
            ),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        TaskCardContent(
            task = task,
            onApprove = { showApproveDialog = true },
            onReject = { showRejectDialog = true }
        )
    }
}

@Composable
private fun TaskCardContent(
    task: Task,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val formattedDate = remember(task.dueDate) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.dueDate))
    }

    val accentColor = when (task.priority) {
        TaskPriority.HIGH -> ProfessionalError
        TaskPriority.MEDIUM -> ProfessionalWarning
        TaskPriority.LOW -> DeepCharcoal
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(accentColor, CircleShape)
                )
                Text(
                    text = task.status.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    ),
                    color = when (task.status) {
                        TaskStatus.TODO -> StoneGray
                        TaskStatus.IN_PROGRESS -> ProfessionalWarning
                        TaskStatus.PENDING_COMPLETION -> Color(0xFF2196F3)
                        TaskStatus.DONE -> ProfessionalSuccess
                    }
                )
            }

            // Priority Badge
            Surface(
                color = accentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = task.priority.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = task.title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Gray900,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Description
        if (task.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = StoneGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
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
                modifier = Modifier.size(14.dp),
                tint = Gray500
            )
            Text(
                text = "DUE: $formattedDate",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = Gray500
            )
        }

        // 🎯 ADMIN APPROVAL SECTION - Show when PENDING_COMPLETION
        if (task.status == TaskStatus.PENDING_COMPLETION) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFF9800).copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF9800))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFFF6F00),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            "APPROVAL REQUIRED",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color(0xFFFF6F00)
                        )
                    }

                    Text(
                        "Employee has completed this task and is waiting for your approval",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepCharcoal.copy(alpha = 0.8f)
                    )

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // APPROVE Button
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfessionalSuccess
                            ),
                            shape = RoundedCornerShape(10.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "APPROVE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        // REJECT Button
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                ProfessionalError
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ProfessionalError
                            )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "REJECT",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
