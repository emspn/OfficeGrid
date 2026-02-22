package com.app.officegrid.employee.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.employee.presentation.common.*
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(
    workspaceId: String = "",
    viewModel: EmployeeDashboardViewModel = hiltViewModel(),
    onTaskClick: (String) -> Unit = {}
) {
    val dashboardState by viewModel.dashboardData.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.syncTasks()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        when (val state = dashboardState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp)
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("ERROR: ${state.message}", color = ProfessionalError, style = MaterialTheme.typography.labelSmall)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AdminSectionHeader(
                title = "Operative Hub",
                subtitle = "MISSION_PERFORMANCE_METRICS"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EliteStatBox(label = "TOTAL_ASSIGNMENTS", value = data.totalTasks.toString(), modifier = Modifier.weight(1f))
                    EliteStatBox(label = "PENDING_OPS", value = data.todoTasks.toString(), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EliteStatBox(label = "IN_DEPLOYMENT", value = data.inProgressTasks.toString(), color = ProfessionalWarning, modifier = Modifier.weight(1f))
                    EliteStatBox(label = "AUTHORIZED_FINALIZE", value = data.completedTasks.toString(), color = ProfessionalSuccess, modifier = Modifier.weight(1f))
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "EFFICIENCY_QUOTIENT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = StoneGray
                        )
                        Text(
                            "${(data.completionRate * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                            color = DeepCharcoal
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { data.completionRate },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = ProfessionalSuccess,
                        trackColor = WarmBorder,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "INCOMING_DATA_STREAM",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = StoneGray
                )

                if (data.recentTasks.isEmpty()) {
                    Text("No active missions detected.", style = MaterialTheme.typography.bodySmall, color = StoneGray)
                } else {
                    data.recentTasks.forEach { task ->
                        EliteDashboardTaskRow(task, onClick = { onTaskClick(task.id) })
                    }
                }
            }
            
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun EliteStatBox(label: String, value: String, color: Color = DeepCharcoal, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black), color = StoneGray)
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace), color = color)
        }
    }
}

@Composable
private fun EliteDashboardTaskRow(task: com.app.officegrid.tasks.domain.model.Task, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val statusColor = when (task.status) {
                TaskStatus.TODO -> StoneGray
                TaskStatus.IN_PROGRESS -> ProfessionalWarning
                TaskStatus.PENDING_COMPLETION -> Color(0xFF2196F3)
                TaskStatus.DONE -> ProfessionalSuccess
            }
            Box(Modifier.size(6.dp).background(statusColor, androidx.compose.foundation.shape.CircleShape))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace), color = DeepCharcoal)
                Text(task.status.name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontFamily = FontFamily.Monospace), color = StoneGray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = WarmBorder, modifier = Modifier.size(16.dp))
        }
    }
}
