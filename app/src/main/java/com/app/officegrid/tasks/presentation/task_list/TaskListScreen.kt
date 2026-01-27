package com.app.officegrid.tasks.presentation.task_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TASK_REGISTRY", style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.sp), color = Gray900)
                        Text("ACTIVE_OPERATIONAL_UNITS", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::syncTasks) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = DeepCharcoal, modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmBackground)
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = WarmBackground
        ) {
            when (val uiState = state) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp, modifier = Modifier.size(24.dp))
                    }
                }
                is UiState.Success -> {
                    val tasks = uiState.data
                    if (tasks.isEmpty()) {
                        EmptyTasksState()
                    } else {
                        PullToRefreshBox(
                            isRefreshing = false,
                            onRefresh = { viewModel.syncTasks() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                state = lazyListState,
                                contentPadding = PaddingValues(24.dp),
                                verticalArrangement = Arrangement.spacedBy(1.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = tasks,
                                    key = { it.id }
                                ) { task ->
                                    EliteTaskRow(
                                        task = task, 
                                        onClick = { viewModel.onTaskClick(task.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorState(message = uiState.message, onRetry = { viewModel.syncTasks() })
                }
            }
        }
    }
}

@Composable
fun EliteTaskRow(task: Task, onClick: () -> Unit) {
    val formattedDate = remember(task.dueDate) { formatDueDate(task.dueDate) }
    
    val accentColor = when (task.priority) {
        TaskPriority.HIGH -> ProfessionalError
        TaskPriority.MEDIUM -> ProfessionalWarning
        TaskPriority.LOW -> DeepCharcoal
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Technical Status Marker
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(6.dp)
                    .background(accentColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.status.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                        color = when(task.status) {
                            TaskStatus.DONE -> ProfessionalSuccess
                            TaskStatus.IN_PROGRESS -> ProfessionalWarning
                            TaskStatus.TODO -> StoneGray
                        }
                    )
                    Text(
                        text = "PRIORITY // ${task.priority.name}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color = Gray900,
                    fontWeight = FontWeight.Bold,
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = Gray500
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DUE: $formattedDate",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                            color = Gray500
                        )
                    }
                    
                    Text(
                        text = "REF_ID: ${task.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                        color = WarmBorder
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyTasksState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Assignment,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = WarmBorder
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "REGISTRY_VACANT",
            style = MaterialTheme.typography.labelSmall,
            color = Gray500
        )
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CONNECTION_FAILURE",
            style = MaterialTheme.typography.labelSmall,
            color = ProfessionalError
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = StoneGray
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(
            onClick = onRetry
        ) {
            Text("RETRY_SYNC", style = MaterialTheme.typography.labelSmall, color = DeepCharcoal)
        }
    }
}

fun formatDueDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
