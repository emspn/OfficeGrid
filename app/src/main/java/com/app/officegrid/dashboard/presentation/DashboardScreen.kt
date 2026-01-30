package com.app.officegrid.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.app.officegrid.analytics.presentation.AnalyticsViewModel
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.ui.theme.DeepCharcoal
import com.app.officegrid.ui.theme.MutedSlate
import com.app.officegrid.ui.theme.ProfessionalError
import com.app.officegrid.ui.theme.ProfessionalSuccess
import com.app.officegrid.ui.theme.ProfessionalWarning
import com.app.officegrid.ui.theme.StoneGray
import com.app.officegrid.ui.theme.WarmBackground
import com.app.officegrid.ui.theme.WarmBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    analyticsViewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val analyticsState by analyticsViewModel.analyticsData.collectAsState()

    // Trigger initial sync when screen loads
    LaunchedEffect(Unit) {
        viewModel.syncData()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        when (val uiState = state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp, modifier = Modifier.size(24.dp))
                }
            }
            is UiState.Success -> {
                if (uiState.data != null) {
                    WarmProfessionalDashboardContent(uiState.data, currentUser, analyticsState, viewModel::syncData)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "No dashboard data available", style = MaterialTheme.typography.bodyLarge, color = ProfessionalError)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.syncData() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "ERROR: ${uiState.message}", style = MaterialTheme.typography.bodyLarge, color = ProfessionalError)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.syncData() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarmProfessionalDashboardContent(
    dashboardData: DashboardData,
    user: User?,
    analyticsState: UiState<com.app.officegrid.analytics.presentation.AnalyticsData>,
    onRefresh: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val companyId = user?.companyId ?: "---"
    
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
            // Header Integrated Title (No separate TopAppBar here anymore)
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text("DASHBOARD", style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Black), color = DeepCharcoal)
                Text("SYSTEM_PERFORMANCE_METRICS", style = MaterialTheme.typography.labelSmall, color = StoneGray)
            }

            // Workspace Identity Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("WORKSPACE_ID", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = companyId,
                            style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 20.sp),
                            color = DeepCharcoal
                        )
                    }
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(companyId)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp), tint = DeepCharcoal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Global Metrics with Progress
            Text("ANALYTICS_OVERVIEW", style = MaterialTheme.typography.labelSmall, color = MutedSlate, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Calculate completion percentage
            val completionRate = if (dashboardData.analytics.totalTasks > 0) {
                dashboardData.analytics.completedTasks.toFloat() / dashboardData.analytics.totalTasks.toFloat()
            } else 0f

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                com.app.officegrid.dashboard.presentation.components.StatCard(
                    label = "TOTAL_TASKS",
                    value = dashboardData.analytics.totalTasks.toString(),
                    modifier = Modifier.weight(1f)
                )
                com.app.officegrid.dashboard.presentation.components.StatCard(
                    label = "COMPLETED",
                    value = dashboardData.analytics.completedTasks.toString(),
                    indicatorColor = ProfessionalSuccess,
                    percentage = completionRate,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                com.app.officegrid.dashboard.presentation.components.StatCard(
                    label = "IN_PROGRESS",
                    value = dashboardData.analytics.inProgressTasks.toString(),
                    indicatorColor = ProfessionalWarning,
                    modifier = Modifier.weight(1f)
                )
                com.app.officegrid.dashboard.presentation.components.StatCard(
                    label = "OVERDUE",
                    value = dashboardData.analytics.overdueTasks.toString(),
                    indicatorColor = ProfessionalError,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Analytics Section - Integrated from AnalyticsScreen
            if (analyticsState is UiState.Success) {
                val analyticsData = analyticsState.data

                // Task Distribution
                Text("TASK_DISTRIBUTION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MutedSlate)
                Spacer(Modifier.height(16.dp))

                TaskDistributionCard(
                    total = analyticsData.totalTasks,
                    todo = analyticsData.todoTasks,
                    inProgress = analyticsData.inProgressTasks,
                    done = analyticsData.completedTasks
                )

                Spacer(Modifier.height(32.dp))

                // Team Performance
                Text("TEAM_PERFORMANCE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MutedSlate)
                Spacer(Modifier.height(16.dp))

                if (analyticsData.teamPerformance.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                    ) {
                        Box(
                            modifier = Modifier.padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No team members yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StoneGray
                            )
                        }
                    }
                } else {
                    analyticsData.teamPerformance.forEach { member ->
                        TeamMemberPerformanceCard(
                            name = member.name,
                            tasksCompleted = member.tasksCompleted,
                            tasksAssigned = member.tasksAssigned,
                            completionRate = member.completionRate
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Priority Breakdown
                Text("PRIORITY_BREAKDOWN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MutedSlate)
                Spacer(Modifier.height(16.dp))

                PriorityBreakdownCard(
                    high = analyticsData.highPriorityTasks,
                    medium = analyticsData.mediumPriorityTasks,
                    low = analyticsData.lowPriorityTasks
                )

                Spacer(Modifier.height(40.dp))
            }

            // User Distribution (Original Dashboard Content)
            Text("TEAM_DISTRIBUTION", style = MaterialTheme.typography.labelSmall, color = MutedSlate, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            dashboardData.performanceList.forEach { performance ->
                WarmEmployeeCard(performance)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun WarmMetricItem(modifier: Modifier, label: String, value: String, indicatorColor: Color? = null) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                indicatorColor?.let {
                    Box(modifier = Modifier.size(6.dp).background(it, CircleShape))
                    Spacer(Modifier.width(8.dp))
                }
                Text(label, style = MaterialTheme.typography.labelSmall, color = StoneGray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value, 
                style = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Monospace, fontSize = 24.sp), 
                color = DeepCharcoal
            )
        }
    }
}

@Composable
fun WarmEmployeeCard(item: PerformanceItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Dot
            Box(modifier = Modifier.size(6.dp).background(ProfessionalSuccess, CircleShape))
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(item.employeeName.uppercase(), style = MaterialTheme.typography.labelMedium, color = DeepCharcoal)
                Text("NODE_REF: ${item.employeeName.take(3).uppercase()}", style = MaterialTheme.typography.labelSmall, color = StoneGray)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${item.completedTasks}/${item.totalTasks}", 
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    color = DeepCharcoal
                )
                val efficiency = if (item.totalTasks > 0) (item.completedTasks * 100 / item.totalTasks) else 0
                Text("$efficiency%_EFF", style = MaterialTheme.typography.labelSmall, color = if (efficiency > 80) ProfessionalSuccess else StoneGray)
            }
        }
    }
}

// Analytics Components Integrated from AnalyticsScreen

@Composable
private fun TaskDistributionCard(
    total: Int,
    todo: Int,
    inProgress: Int,
    done: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Total: $total tasks",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepCharcoal
            )

            Spacer(Modifier.height(20.dp))

            StatusBar(label = "TODO", count = todo, total = total, color = StoneGray)
            Spacer(Modifier.height(12.dp))
            StatusBar(label = "IN PROGRESS", count = inProgress, total = total, color = ProfessionalWarning)
            Spacer(Modifier.height(12.dp))
            StatusBar(label = "DONE", count = done, total = total, color = ProfessionalSuccess)
        }
    }
}

@Composable
private fun StatusBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DeepCharcoal
            )
            Text(
                "$count / $total",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = StoneGray
            )
        }

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { if (total > 0) count.toFloat() / total.toFloat() else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = WarmBorder,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun TeamMemberPerformanceCard(
    name: String,
    tasksCompleted: Int,
    tasksAssigned: Int,
    completionRate: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = DeepCharcoal,
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.first().toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepCharcoal
                )
                Text(
                    "$tasksCompleted / $tasksAssigned tasks",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = StoneGray
                )
            }

            Surface(
                color = when {
                    completionRate >= 0.8f -> ProfessionalSuccess.copy(alpha = 0.1f)
                    completionRate >= 0.5f -> ProfessionalWarning.copy(alpha = 0.1f)
                    else -> ProfessionalError.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "${(completionRate * 100).toInt()}%",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = when {
                        completionRate >= 0.8f -> ProfessionalSuccess
                        completionRate >= 0.5f -> ProfessionalWarning
                        else -> ProfessionalError
                    }
                )
            }
        }
    }
}

@Composable
private fun PriorityBreakdownCard(
    high: Int,
    medium: Int,
    low: Int
) {
    val total = high + medium + low

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PriorityColumn(
                label = "HIGH",
                count = high,
                total = total,
                color = ProfessionalError,
                modifier = Modifier.weight(1f)
            )
            PriorityColumn(
                label = "MEDIUM",
                count = medium,
                total = total,
                color = ProfessionalWarning,
                modifier = Modifier.weight(1f)
            )
            PriorityColumn(
                label = "LOW",
                count = low,
                total = total,
                color = DeepCharcoal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PriorityColumn(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            count.toString(),
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            ),
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = StoneGray
        )

        Spacer(Modifier.height(8.dp))

        val percentage = if (total > 0) (count.toFloat() / total.toFloat() * 100).toInt() else 0
        Text(
            "$percentage%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}

