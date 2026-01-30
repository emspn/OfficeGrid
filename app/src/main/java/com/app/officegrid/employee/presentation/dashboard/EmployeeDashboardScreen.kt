package com.app.officegrid.employee.presentation.dashboard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.ui.theme.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(
    viewModel: EmployeeDashboardViewModel = hiltViewModel(),
    onTaskClick: (String) -> Unit = {}
) {
    val dashboardState by viewModel.dashboardData.collectAsState()

    // Auto-sync when screen loads
    LaunchedEffect(Unit) {
        viewModel.syncTasks()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        when (val state = dashboardState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp, modifier = Modifier.size(24.dp))
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Failed to load dashboard", style = MaterialTheme.typography.bodyLarge, color = ProfessionalError)
                        Spacer(Modifier.height(8.dp))
                        Text(text = state.message, style = MaterialTheme.typography.bodySmall, color = StoneGray)
                    }
                }
            }
            is UiState.Success -> {
                EmployeeDashboardContent(
                    data = state.data,
                    onTaskClick = onTaskClick,
                    onRefresh = { viewModel.syncTasks() }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeDashboardContent(
    data: EmployeeDashboardData,
    onTaskClick: (String) -> Unit,
    onRefresh: () -> Unit = {}
) {
    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    "MY_DASHBOARD",
                    style = MaterialTheme.typography.titleLarge.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = DeepCharcoal
                )
                Text(
                    "YOUR_ASSIGNED_TASKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGray
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "TOTAL",
                    value = data.totalTasks.toString(),
                    icon = Icons.Default.Assignment,
                    color = DeepCharcoal,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "TODO",
                    value = data.todoTasks.toString(),
                    icon = Icons.Default.RadioButtonUnchecked,
                    color = StoneGray,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "IN_PROGRESS",
                    value = data.inProgressTasks.toString(),
                    icon = Icons.Default.HourglassEmpty,
                    color = ProfessionalWarning,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "COMPLETED",
                    value = data.completedTasks.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = ProfessionalSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(32.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "COMPLETION_RATE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MutedSlate
                        )
                        Text(
                            (data.completionRate * 100).toInt().toString() + "%",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = ProfessionalSuccess
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { data.completionRate },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = ProfessionalSuccess,
                        trackColor = WarmBorder
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RECENT_TASKS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MutedSlate
                )
                Text(
                    data.recentTasks.size.toString() + " tasks",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGray
                )
            }
            Spacer(Modifier.height(16.dp))
            if (data.recentTasks.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Box(
                        modifier = Modifier.padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.size(64.dp),
                                color = StoneGray.copy(alpha = 0.1f),
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("??", style = MaterialTheme.typography.displaySmall)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "NO_TASKS_ASSIGNED",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepCharcoal
                            )
                            Text(
                                "Check back later",
                                style = MaterialTheme.typography.labelSmall,
                                color = StoneGray
                            )
                        }
                    }
                }
            } else {
                data.recentTasks.forEach { task ->
                    TaskCard(task = task, onClick = { onTaskClick(task.id) })
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
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
private fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    val statusColor = when (task.status) {
        TaskStatus.TODO -> StoneGray
        TaskStatus.IN_PROGRESS -> ProfessionalWarning
        TaskStatus.DONE -> ProfessionalSuccess
    }
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> ProfessionalError
        TaskPriority.MEDIUM -> ProfessionalWarning
        TaskPriority.LOW -> DeepCharcoal
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                color = statusColor,
                shape = CircleShape
            ) {}
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal,
                        maxLines = 1
                    )
                    Surface(
                        color = priorityColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = task.priority.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = priorityColor
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = StoneGray,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = StoneGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
