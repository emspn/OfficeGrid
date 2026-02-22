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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(
    task: Task,
    userRole: UserRole,
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

    val isOverdue = remember(task.dueDate, task.status) {
        task.dueDate < System.currentTimeMillis() && task.status != TaskStatus.DONE
    }

    // ✨ MODERN APPROVAL DIALOG
    if (showApproveDialog) {
        EliteTaskActionDialog(
            title = "Approve Completion",
            message = "Confirm that this task has been successfully finalized. This will mark the objective as DONE.",
            taskTitle = task.title,
            confirmLabel = "APPROVE_TASK",
            icon = Icons.Default.CheckCircle,
            accentColor = ProfessionalSuccess,
            onConfirm = {
                onStatusChange(TaskStatus.DONE)
                showApproveDialog = false
            },
            onDismiss = { showApproveDialog = false }
        )
    }

    // ✨ MODERN REJECTION DIALOG
    if (showRejectDialog) {
        EliteTaskActionDialog(
            title = "Reject Completion",
            message = "This task does not meet operational standards. Reverting status to IN_PROGRESS for further refinement.",
            taskTitle = task.title,
            confirmLabel = "REJECT_TASK",
            icon = Icons.Default.Cancel,
            accentColor = ProfessionalError,
            onConfirm = {
                onStatusChange(TaskStatus.IN_PROGRESS)
                showRejectDialog = false
            },
            onDismiss = { showRejectDialog = false }
        )
    }

    // ✨ MODERN DELETE DIALOG
    if (showDeleteDialog) {
        EliteTaskActionDialog(
            title = "Delete Assignment",
            message = "Permanently remove this task from the operational registry? This action is irreversible.",
            taskTitle = task.title,
            confirmLabel = "DELETE_PERMANENTLY",
            icon = Icons.Default.DeleteForever,
            accentColor = ProfessionalError,
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Task Card UI
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { if (userRole == UserRole.ADMIN) showOptionsMenu = true }
            ),
        color = if (isOverdue) Color(0xFFFFEBEE) else Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isOverdue) 1.5.dp else 0.5.dp, 
            color = if (isOverdue) ProfessionalError.copy(0.5f) else WarmBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(getStatusColor(task.status), CircleShape))
                    Text(
                        text = if (isOverdue) "OVERDUE" else task.status.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                        color = if (isOverdue) ProfessionalError else getStatusColor(task.status)
                    )
                }
                
                Surface(color = getPriorityColor(task.priority).copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = task.priority.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                        color = getPriorityColor(task.priority)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepCharcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(12.dp), tint = StoneGray)
                    Text(
                        text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(task.dueDate)),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = if (isOverdue) ProfessionalError else StoneGray
                    )
                }

                if (task.status == TaskStatus.PENDING_COMPLETION && userRole == UserRole.ADMIN) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showRejectDialog = true }, modifier = Modifier.size(32.dp).background(ProfessionalError.copy(0.1f), CircleShape)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = ProfessionalError)
                        }
                        IconButton(onClick = { showApproveDialog = true }, modifier = Modifier.size(32.dp).background(ProfessionalSuccess.copy(0.1f), CircleShape)) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = ProfessionalSuccess)
                        }
                    }
                } else if (task.status == TaskStatus.PENDING_COMPLETION) {
                    Text("AWAITING_REVIEW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF2196F3)))
                }
            }
        }
    }

    if (showOptionsMenu) {
        ModalBottomSheet(onDismissRequest = { showOptionsMenu = false }, containerColor = Color.White) {
            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                Text("TASK_OPERATIONS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), modifier = Modifier.padding(bottom = 16.dp))
                ListItem(
                    headlineContent = { Text("Edit Task") },
                    leadingContent = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.combinedClickable(onClick = { onEdit?.invoke(); showOptionsMenu = false })
                )
                ListItem(
                    headlineContent = { Text("Delete Task", color = ProfessionalError) },
                    leadingContent = { Icon(Icons.Default.Delete, null, tint = ProfessionalError) },
                    modifier = Modifier.combinedClickable(onClick = { showDeleteDialog = true; showOptionsMenu = false })
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun EliteTaskActionDialog(
    title: String,
    message: String,
    taskTitle: String,
    confirmLabel: String,
    icon: ImageVector,
    accentColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = accentColor, modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Title
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = DeepCharcoal,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                // Task Details context
                Surface(
                    color = WarmBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = taskTitle,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Gray700,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Message
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StoneGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(32.dp))

                // Action Buttons - Stacked for clarity or Side-by-side
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = confirmLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                    ) {
                        Text(
                            text = "CANCEL_OPERATION",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = StoneGray
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun getStatusColor(status: TaskStatus) = when (status) {
    TaskStatus.TODO -> StoneGray
    TaskStatus.IN_PROGRESS -> ProfessionalWarning
    TaskStatus.PENDING_COMPLETION -> Color(0xFF2196F3)
    TaskStatus.DONE -> ProfessionalSuccess
}

private fun getPriorityColor(priority: TaskPriority) = when (priority) {
    TaskPriority.HIGH -> ProfessionalError
    TaskPriority.MEDIUM -> ProfessionalWarning
    TaskPriority.LOW -> DeepCharcoal
}
