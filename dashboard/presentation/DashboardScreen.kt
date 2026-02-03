package com.app.officegrid.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.core.ui.AdminStatCard
import com.app.officegrid.employee.presentation.common.EmployeeLoadingIndicator
import com.app.officegrid.employee.presentation.common.EmployeeErrorSurface
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Sync on screen resume
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.syncData()
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
        when (val uiState = state) {
            is UiState.Loading -> {
                EmployeeLoadingIndicator()
            }
            is UiState.Success -> {
                AdminDashboardContent(
                    dashboardData = uiState.data,
                    user = currentUser,
                    onRefresh = viewModel::syncData
                )
            }
            is UiState.Error -> {
                EmployeeErrorSurface(
                    message = "DASHBOARD_SYNC_FAILURE",
                    details = uiState.message,
                    onRetry = { viewModel.syncData() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDashboardContent(
    dashboardData: DashboardData,
    user: User?,
    onRefresh: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val companyId = user?.companyId ?: "---"
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }
    
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
            // Header
            AdminSectionHeader(
                title = "SYSTEM_OVERVIEW",
                subtitle = "REAL-TIME_ORGANIZATION_METRICS"
            )

            // Join Code Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DeepCharcoal,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("WORKSPACE_JOIN_CODE", style = MaterialTheme.typography.labelSmall, color = ProfessionalSuccess)
                        Text(
                            text = companyId,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = { 
                            clipboardManager.setText(AnnotatedString(companyId))
                            isCopied = true
                        }
                    ) {
                        Icon(
                            if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy, 
                            null, 
                            modifier = Modifier.size(20.dp),
                            tint = if (isCopied) ProfessionalSuccess else Color.White
                        )
                    }
                }
            }

            // Quick Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("GLOBAL_METRICS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = StoneGray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard(
                        label = "TOTAL_TASKS",
                        value = dashboardData.analytics.totalTasks.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "COMPLETED",
                        value = dashboardData.analytics.completedTasks.toString(),
                        indicatorColor = ProfessionalSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard(
                        label = "IN_PROGRESS",
                        value = dashboardData.analytics.inProgressTasks.toString(),
                        indicatorColor = ProfessionalWarning,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "OVERDUE",
                        value = dashboardData.analytics.overdueTasks.toString(),
                        indicatorColor = ProfessionalError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Task Distribution Progress
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("WORKFLOW_DISTRIBUTION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = StoneGray)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val total = dashboardData.analytics.totalTasks
                        AdminProgressBar("PENDING", dashboardData.analytics.pendingTasks, total, StoneGray)
                        AdminProgressBar("IN_PROGRESS", dashboardData.analytics.inProgressTasks, total, ProfessionalWarning)
                        AdminProgressBar("COMPLETED", dashboardData.analytics.completedTasks, total, ProfessionalSuccess)
                    }
                }
            }

            // Team Efficiency List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("TEAM_EFFICIENCY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = StoneGray)
                if (dashboardData.teamPerformance.isEmpty()) {
                    Text("No active operatives found.", style = MaterialTheme.typography.bodySmall, color = StoneGray, modifier = Modifier.padding(start = 4.dp))
                } else {
                    dashboardData.teamPerformance.forEach { member ->
                        AdminTeamCard(member)
                    }
                }
            }
            
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun AdminProgressBar(label: String, count: Int, total: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
            Text("$count tasks", style = MaterialTheme.typography.labelSmall, color = StoneGray)
        }
        LinearProgressIndicator(
            progress = { if (total > 0) count.toFloat() / total.toFloat() else 0f },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = WarmBorder,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun AdminTeamCard(member: PerformanceItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(modifier = Modifier.size(36.dp), color = DeepCharcoal, shape = CircleShape) {
                Box(contentAlignment = Alignment.Center) {
                    Text(member.employeeName.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(member.employeeName.uppercase(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                Text("${member.tasksCompleted}/${member.tasksAssigned} COMPLETED", style = MaterialTheme.typography.labelSmall, color = StoneGray)
            }
            Text(
                "${(member.completionRate * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                color = if (member.completionRate > 0.8) ProfessionalSuccess else StoneGray
            )
        }
    }
}
