package com.app.officegrid.team.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Team Management", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Manage your workforce", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::syncTeam) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = PrimaryModern)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = viewModel::syncTeam,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Pending Requests Section
                    if (state.pendingRequests.isNotEmpty()) {
                        item {
                            ModernSectionHeader(title = "JOIN REQUESTS", count = state.pendingRequests.size, color = WarningModern)
                        }
                        items(state.pendingRequests) { employee ->
                            ModernPendingItem(
                                employee = employee,
                                onApprove = { viewModel.approveEmployee(employee.id) },
                                onReject = { viewModel.rejectEmployee(employee.id) }
                            )
                        }
                    }

                    // 2. Team Members Section
                    item {
                        ModernSectionHeader(title = "TEAM MEMBERS", count = state.approvedMembers.size, color = PrimaryModern)
                    }

                    if (state.approvedMembers.isEmpty() && state.pendingRequests.isEmpty()) {
                        item {
                            ModernEmptyTeamState()
                        }
                    } else {
                        items(state.approvedMembers) { employee ->
                            ModernTeamMemberItem(employee = employee)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernSectionHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = color,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.weight(2f), color = color.copy(alpha = 0.1f))
    }
}

@Composable
fun ModernPendingItem(
    employee: Employee,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                color = WarningModern.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = employee.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = WarningModern
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = employee.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Slate900)
                Text(text = employee.email, style = MaterialTheme.typography.labelMedium, color = Slate500)
            }

            Row {
                IconButton(
                    onClick = onApprove,
                    modifier = Modifier.background(SuccessModern.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", tint = SuccessModern)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onReject,
                    modifier = Modifier.background(ErrorModern.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = ErrorModern)
                }
            }
        }
    }
}

@Composable
fun ModernTeamMemberItem(employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                color = PrimaryModern.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = employee.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryModern
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = employee.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Slate900)
                Text(text = employee.email, style = MaterialTheme.typography.labelMedium, color = Slate500)
            }
        }
    }
}

@Composable
fun ModernEmptyTeamState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            color = PrimaryModern.copy(alpha = 0.05f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Groups, 
                    contentDescription = null, 
                    modifier = Modifier.size(48.dp),
                    tint = PrimaryModern.copy(alpha = 0.3f)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your team is empty",
            style = MaterialTheme.typography.titleLarge,
            color = Slate900
        )
        Text(
            text = "Approved members will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500
        )
    }
}