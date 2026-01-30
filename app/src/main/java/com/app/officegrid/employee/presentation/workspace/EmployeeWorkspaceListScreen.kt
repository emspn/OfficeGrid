package com.app.officegrid.employee.presentation.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.ui.theme.*

data class Workspace(
    val id: String,
    val name: String,
    val industry: String,
    val status: WorkspaceStatus,
    val taskCount: Int = 0
)

enum class WorkspaceStatus {
    ACTIVE, PENDING, REJECTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeWorkspaceListScreen(
    onWorkspaceClick: (String) -> Unit,
    onJoinWorkspace: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: EmployeeWorkspaceViewModel = hiltViewModel()
) {
    val workspaces by viewModel.workspaces.collectAsState()
    val userName by viewModel.userName.collectAsState()

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "MY_WORKSPACES",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = DeepCharcoal
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = DeepCharcoal,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = WarmBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onJoinWorkspace,
                containerColor = DeepCharcoal,
                contentColor = Color.White,
                icon = {
                    Icon(Icons.Default.Add, contentDescription = null)
                },
                text = {
                    Text(
                        "JOIN_WORKSPACE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // User Welcome Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(DeepCharcoal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "E",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back,",
                            style = MaterialTheme.typography.labelSmall,
                            color = StoneGray
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = DeepCharcoal
                        )
                    }
                }
            }

            // Workspace List
            if (workspaces.isEmpty()) {
                EmptyWorkspaceState(onJoinWorkspace = onJoinWorkspace)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(workspaces) { workspace ->
                        WorkspaceCard(
                            workspace = workspace,
                            onClick = { onWorkspaceClick(workspace.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWorkspaceState(onJoinWorkspace: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(
                Icons.Default.BusinessCenter,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = WarmBorder
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "NO_WORKSPACES_YET",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = DeepCharcoal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Join your first workspace to start",
                style = MaterialTheme.typography.bodySmall,
                color = StoneGray
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onJoinWorkspace,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepCharcoal
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "JOIN_WORKSPACE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun WorkspaceCard(
    workspace: Workspace,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        when (workspace.status) {
                            WorkspaceStatus.ACTIVE -> ProfessionalSuccess.copy(alpha = 0.1f)
                            WorkspaceStatus.PENDING -> ProfessionalWarning.copy(alpha = 0.1f)
                            WorkspaceStatus.REJECTED -> ProfessionalError.copy(alpha = 0.1f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (workspace.status) {
                        WorkspaceStatus.ACTIVE -> Icons.Default.CheckCircle
                        WorkspaceStatus.PENDING -> Icons.Default.Schedule
                        WorkspaceStatus.REJECTED -> Icons.Default.Cancel
                    },
                    contentDescription = null,
                    tint = when (workspace.status) {
                        WorkspaceStatus.ACTIVE -> ProfessionalSuccess
                        WorkspaceStatus.PENDING -> ProfessionalWarning
                        WorkspaceStatus.REJECTED -> ProfessionalError
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workspace.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = DeepCharcoal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = workspace.industry,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = StoneGray
                    )
                    if (workspace.status == WorkspaceStatus.ACTIVE) {
                        Text(
                            text = " • ",
                            color = StoneGray
                        )
                        Text(
                            text = "${workspace.taskCount} tasks",
                            style = MaterialTheme.typography.labelSmall,
                            color = StoneGray
                        )
                    }
                }
                if (workspace.status != WorkspaceStatus.ACTIVE) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (workspace.status) {
                            WorkspaceStatus.PENDING -> "Waiting for approval"
                            WorkspaceStatus.REJECTED -> "Access denied"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = when (workspace.status) {
                            WorkspaceStatus.PENDING -> ProfessionalWarning
                            WorkspaceStatus.REJECTED -> ProfessionalError
                            else -> StoneGray
                        }
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = WarmBorder
            )
        }
    }
}
