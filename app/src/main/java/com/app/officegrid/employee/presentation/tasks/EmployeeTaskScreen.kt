package com.app.officegrid.employee.presentation.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.presentation.task_list.TaskListViewModel
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.launch

/**
 * ✨ EMPLOYEE TASK SCREEN - Professional UI
 * Shows tasks with swipe-to-update status functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeTaskScreen(
    workspaceId: String,
    onTaskClick: (String) -> Unit = {},
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val tasksState by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Filter tasks by workspace
    val filteredTasks = when (val state = tasksState) {
        is UiState.Success -> state.data.filter { it.companyId == workspaceId }
        else -> emptyList()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmBackground
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = WarmBackground
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        viewModel.syncTasks()
                        kotlinx.coroutines.delay(1000)
                        isRefreshing = false
                    }
                }
            ) {
            when (tasksState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = DeepCharcoal,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = ProfessionalError
                            )
                            Text(
                                "Error loading tasks",
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepCharcoal
                            )
                            Text(
                                (tasksState as UiState.Error).message,
                                style = MaterialTheme.typography.bodySmall,
                                color = StoneGray
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    if (filteredTasks.isEmpty()) {
                        EmptyTasksState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredTasks, key = { it.id }) { task ->
                                SwipeableTaskCard(
                                    task = task,
                                    onClick = { onTaskClick(task.id) },
                                    onStatusChange = { newStatus ->
                                        scope.launch {
                                            try {
                                                viewModel.updateTaskStatus(task.id, newStatus)
                                                snackbarHostState.showSnackbar(
                                                    message = "Status updated to ${newStatus.name.replace("_", " ")}!",
                                                    duration = SnackbarDuration.Short
                                                )
                                                // Refresh to show updated task
                                                viewModel.syncTasks()
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar(
                                                    message = "Failed to update: ${e.message}",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(
    task: Task,
    onClick: () -> Unit = {},
    onStatusChange: (TaskStatus) -> Unit
) {
    var showStatusDialog by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title + Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = DeepCharcoal,
                    modifier = Modifier.weight(1f)
                )

                // Priority badge
                Surface(
                    color = when (task.priority) {
                        TaskPriority.HIGH -> ProfessionalError.copy(alpha = 0.1f)
                        TaskPriority.MEDIUM -> ProfessionalWarning.copy(alpha = 0.1f)
                        TaskPriority.LOW -> Color(0xFF64B5F6).copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        task.priority.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = when (task.priority) {
                            TaskPriority.HIGH -> ProfessionalError
                            TaskPriority.MEDIUM -> ProfessionalWarning
                            TaskPriority.LOW -> Color(0xFF2196F3)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Description
            if (task.description.isNotBlank()) {
                Text(
                    task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StoneGray,
                    lineHeight = 20.sp
                )
            }

            // Status + Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge with click hint
                Surface(
                    onClick = { showStatusDialog = true },
                    color = when (task.status) {
                        TaskStatus.TODO -> ProfessionalWarning.copy(alpha = 0.1f)
                        TaskStatus.IN_PROGRESS -> Color(0xFF2196F3).copy(alpha = 0.1f)
                        TaskStatus.PENDING_COMPLETION -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        TaskStatus.DONE -> ProfessionalSuccess.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (task.status) {
                                TaskStatus.TODO -> Icons.Default.RadioButtonUnchecked
                                TaskStatus.IN_PROGRESS -> Icons.Default.HourglassEmpty
                                TaskStatus.PENDING_COMPLETION -> Icons.Default.Send
                                TaskStatus.DONE -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = when (task.status) {
                                TaskStatus.TODO -> ProfessionalWarning
                                TaskStatus.IN_PROGRESS -> Color(0xFF2196F3)
                                TaskStatus.PENDING_COMPLETION -> Color(0xFFFF9800)
                                TaskStatus.DONE -> ProfessionalSuccess
                            }
                        )
                        Text(
                            task.status.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = when (task.status) {
                                TaskStatus.TODO -> ProfessionalWarning
                                TaskStatus.IN_PROGRESS -> Color(0xFF2196F3)
                                TaskStatus.PENDING_COMPLETION -> Color(0xFFFF9800)
                                TaskStatus.DONE -> ProfessionalSuccess
                            }
                        )
                    }
                }

                // Due date
                Text(
                    "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = StoneGray
                )
            }
        }
    }

    // Status update dialog
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = {
                Text(
                    "Update Task Status",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StoneGray
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    TaskStatus.values().forEach { status ->
                        Surface(
                            onClick = {
                                onStatusChange(status)
                                showStatusDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (status == task.status) {
                                DeepCharcoal.copy(alpha = 0.1f)
                            } else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when (status) {
                                        TaskStatus.TODO -> Icons.Default.RadioButtonUnchecked
                                        TaskStatus.IN_PROGRESS -> Icons.Default.HourglassEmpty
                                        TaskStatus.PENDING_COMPLETION -> Icons.Default.Send
                                        TaskStatus.DONE -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = when (status) {
                                        TaskStatus.TODO -> ProfessionalWarning
                                        TaskStatus.IN_PROGRESS -> Color(0xFF2196F3)
                                        TaskStatus.PENDING_COMPLETION -> Color(0xFFFF9800)
                                        TaskStatus.DONE -> ProfessionalSuccess
                                    }
                                )
                                Text(
                                    status.name.replace("_", " "),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (status == task.status) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun EmptyTasksState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = StoneGray.copy(alpha = 0.4f)
            )
            Text(
                "No Tasks Yet",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = DeepCharcoal
            )
            Text(
                "Tasks assigned by your admin will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = StoneGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

