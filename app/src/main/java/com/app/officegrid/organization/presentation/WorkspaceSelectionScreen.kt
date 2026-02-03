package com.app.officegrid.organization.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.model.EmployeeStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSelectionScreen(
    viewModel: WorkspaceListViewModel = hiltViewModel(),
    onWorkspaceSelected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Select Workspace", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.workspaces.isEmpty()) {
                Text(
                    "You haven't joined any workspaces yet.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.workspaces) { workspace ->
                        WorkspaceItem(
                            workspace = workspace,
                            onClick = {
                                viewModel.selectWorkspace(workspace)
                                onWorkspaceSelected()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceItem(
    workspace: Employee,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // We'll use the ID as a fallback name since we'd need to fetch Org name separately
                Text(
                    text = "Workspace: ${workspace.companyId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Role: ${workspace.role}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (workspace.status == EmployeeStatus.APPROVED) {
                Icon(Icons.Default.CheckCircle, "Approved", tint = Color(0xFF4CAF50))
            } else {
                Icon(Icons.Default.Pending, "Pending", tint = Color(0xFFFFA000))
            }
        }
    }
}
