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
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-sync when screen loads
    LaunchedEffect(Unit) {
        viewModel.syncTeam()
    }

    // Show success/error messages
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = "Error: $error",
                duration = SnackbarDuration.Long
            )
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = WarmBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = WarmBackground
        ) {
            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = viewModel::syncTeam,
                modifier = Modifier.fillMaxSize()
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(bottom = 32.dp)) {
                        Text("NODE_MANAGEMENT", style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Black), color = DeepCharcoal)
                        Text("SYSTEM_OPERATIVE_REGISTRY", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                    }
                }

                // 1. Pending Requests Section
                if (state.pendingRequests.isNotEmpty()) {
                    item {
                        EliteTeamSectionHeader(title = "PENDING_NODE_REQUESTS", count = state.pendingRequests.size, color = ProfessionalWarning)
                    }
                    items(state.pendingRequests) { employee ->
                        ElitePendingOperativeRow(
                            employee = employee,
                            onApprove = { viewModel.approveEmployee(employee.id) },
                            onReject = { viewModel.rejectEmployee(employee.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }

                // 2. Team Members Section
                item {
                    EliteTeamSectionHeader(title = "APPROVED_OPERATIVE_REGISTRY", count = state.approvedMembers.size, color = DeepCharcoal)
                }

                if (state.approvedMembers.isEmpty() && state.pendingRequests.isEmpty()) {
                    item {
                        EliteEmptyTeamState()
                    }
                } else {
                    items(state.approvedMembers) { employee ->
                        EliteOperativeRow(employee = employee)
                    }
                }
            }
        }
    }
    }
}

@Composable
fun EliteTeamSectionHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = color
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "[ ${count.toString().padStart(2, '0')} ]",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = StoneGray
        )
    }
}

@Composable
fun ElitePendingOperativeRow(
    employee: Employee,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).background(ProfessionalWarning, CircleShape))
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = employee.name.uppercase(), style = MaterialTheme.typography.labelMedium, color = DeepCharcoal)
                Text(text = employee.email, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp), color = StoneGray)
            }

            Row {
                IconButton(
                    onClick = onApprove,
                    modifier = Modifier.size(32.dp).background(ProfessionalSuccess.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", tint = ProfessionalSuccess, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onReject,
                    modifier = Modifier.size(32.dp).background(ProfessionalError.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = ProfessionalError, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun EliteOperativeRow(employee: Employee) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).background(ProfessionalSuccess, CircleShape))
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = employee.name.uppercase(), style = MaterialTheme.typography.labelMedium, color = DeepCharcoal)
                Text(text = "NODE_REF: ${employee.id.take(8).uppercase()}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp), color = StoneGray)
            }
            
            Text(
                text = "ACTIVE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                color = ProfessionalSuccess
            )
        }
    }
}

@Composable
fun EliteEmptyTeamState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Groups, 
            contentDescription = null, 
            modifier = Modifier.size(48.dp),
            tint = WarmBorder
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "NO_OPERATIVES_REGISTERED",
            style = MaterialTheme.typography.labelSmall,
            color = StoneGray
        )
    }
}
