package com.app.officegrid.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
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
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("DASHBOARD", style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.sp), color = DeepCharcoal)
                        Text("SYSTEM_PERFORMANCE_METRICS", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::syncData) {
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
                    uiState.data?.let {
                        WarmProfessionalDashboardContent(it, currentUser, viewModel::syncData)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "CRITICAL_ERROR: ${uiState.message}", style = MaterialTheme.typography.labelSmall, color = ProfessionalError)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarmProfessionalDashboardContent(data: DashboardData, user: User?, onRefresh: () -> Unit) {
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
            // Workspace Identity Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = WarmSurface,
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

            // Global Metrics
            Text("ANALYTICS_OVERVIEW", style = MaterialTheme.typography.labelSmall, color = MutedSlate, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WarmMetricItem(Modifier.weight(1f), "TOTAL_ASSIGNMENTS", data.analytics.totalTasks.toString())
                WarmMetricItem(Modifier.weight(1f), "COMPLETED_UNITS", data.analytics.completedTasks.toString(), ProfessionalSuccess)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WarmMetricItem(Modifier.weight(1f), "ACTIVE_SESSIONS", data.analytics.inProgressTasks.toString(), ProfessionalWarning)
                WarmMetricItem(Modifier.weight(1f), "OVERDUE_FLAGS", data.analytics.overdueTasks.toString(), ProfessionalError)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // User Distribution
            Text("TEAM_DISTRIBUTION", style = MaterialTheme.typography.labelSmall, color = MutedSlate, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            data.performanceList.forEach { performance ->
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
        color = WarmSurface,
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
        color = WarmSurface,
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