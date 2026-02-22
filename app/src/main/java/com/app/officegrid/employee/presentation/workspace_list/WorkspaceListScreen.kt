package com.app.officegrid.employee.presentation.workspace_list

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.core.ui.components.OfficeGridLogo
import com.app.officegrid.employee.presentation.common.*
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WorkspaceListScreen(
    onWorkspaceClick: (String) -> Unit,
    onAddWorkspace: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: WorkspaceViewModel = hiltViewModel()
) {
    val workspacesState by viewModel.workspaces.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var selectedWorkspaceForLeave by remember { mutableStateOf<WorkspaceItem?>(null) }
    
    val haptic = LocalHapticFeedback.current
    val refreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = WarmBackground
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = WarmBackground
        ) {
            // ✅ FIX: Use isRefreshing state so data stays visible while updating
            PullToRefreshBox(
                state = refreshState,
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshWorkspaces() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = workspacesState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 2.dp)
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("ERROR: ${state.message}", color = ProfessionalError)
                        }
                    }
                    is UiState.Success -> {
                        val workspaces = state.data
                        if (workspaces.isEmpty()) {
                            EliteEmptyWorkspacesState(onAddWorkspace = onAddWorkspace)
                        } else {
                            val activeWorkspaces = workspaces.filter { it.status == WorkspaceStatus.ACTIVE }
                            val pendingRequests = workspaces.filter { it.status == WorkspaceStatus.PENDING }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OfficeGridLogo(size = 32.dp)
                                            Spacer(Modifier.width(12.dp))
                                            AdminSectionHeader(title = "Registry", subtitle = "OPERATIONAL_NODES")
                                        }
                                        
                                        FilledIconButton(
                                            onClick = onAddWorkspace,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = DeepCharcoal)
                                        ) {
                                            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }

                                if (pendingRequests.isNotEmpty()) {
                                    item {
                                        Text("PENDING_VERIFICATION", 
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp), 
                                            color = StoneGray
                                        )
                                    }
                                    items(pendingRequests, key = { it.companyId }) { workspace ->
                                        EliteWorkspaceRow(
                                            workspace = workspace,
                                            onClick = { },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectedWorkspaceForLeave = workspace
                                            }
                                        )
                                    }
                                }

                                item {
                                    Text("ACTIVE_CHANNELS", 
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp), 
                                        color = DeepCharcoal
                                    )
                                }

                                items(activeWorkspaces, key = { it.companyId }) { workspace ->
                                    EliteWorkspaceRow(
                                        workspace = workspace,
                                        onClick = {
                                            viewModel.selectWorkspace(workspace)
                                            onWorkspaceClick(workspace.companyId)
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedWorkspaceForLeave = workspace
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedWorkspaceForLeave != null) {
        AlertDialog(
            onDismissRequest = { selectedWorkspaceForLeave = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ProfessionalError) },
            title = { Text("TERMINATE_CONNECTION?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
            text = { 
                Text("Are you sure you want to disconnect from node \"${selectedWorkspaceForLeave?.name}\"? This action will revoke all security clearances.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedWorkspaceForLeave?.let { viewModel.leaveWorkspace(it.companyId) }
                        selectedWorkspaceForLeave = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalError)
                ) {
                    Text("DISCONNECT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedWorkspaceForLeave = null }) {
                    Text("CANCEL", color = StoneGray)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EliteWorkspaceRow(
    workspace: WorkspaceItem, 
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isActive = workspace.status == WorkspaceStatus.ACTIVE
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(if (isActive) ProfessionalSuccess else ProfessionalWarning, CircleShape))
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(workspace.name.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), color = DeepCharcoal)
                Text(text = "NODE: ${workspace.companyId}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace), color = StoneGray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = WarmBorder, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun EliteEmptyWorkspacesState(onAddWorkspace: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        OfficeGridLogo(size = 80.dp, modifier = Modifier.alpha(0.2f))
        Spacer(modifier = Modifier.height(32.dp))
        Text("NO_NODES_FOUND", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black), color = DeepCharcoal)
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = onAddWorkspace, colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)) {
            Text("CONNECT_NEW_NODE")
        }
    }
}
