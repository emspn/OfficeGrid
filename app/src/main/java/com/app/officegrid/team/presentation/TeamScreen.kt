package com.app.officegrid.team.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedEmployeeForOptions by remember { mutableStateOf<Employee?>(null) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }

    // Show success/error messages
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = WarmBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(WarmBackground)
        ) {
            PullToRefreshBox(
                isRefreshing = false, // Manual refresh only to avoid constant arrow
                onRefresh = viewModel::syncTeam,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    item {
                        Column(modifier = Modifier.padding(bottom = 24.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                AdminSectionHeader(
                                    title = "Team Management",
                                    subtitle = "Manage your team members"
                                )
                                IconButton(onClick = viewModel::syncTeam) {
                                    Icon(Icons.Default.Refresh, null, tint = DeepCharcoal)
                                }
                            }
                        }
                    }

                    // 1. Pending Requests Section
                    if (state.pendingRequests.isNotEmpty()) {
                        item {
                            EliteTeamSectionHeader(title = "Pending Requests", count = state.pendingRequests.size, color = ProfessionalWarning)
                        }
                        items(state.pendingRequests) { employee ->
                            ElitePendingOperativeRow(
                                employee = employee,
                                onApprove = { viewModel.approveEmployee(employee.id) },
                                onReject = { viewModel.removeEmployee(employee.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }

                    // 2. Team Members Section
                    item {
                        EliteTeamSectionHeader(title = "Team Members", count = state.approvedMembers.size, color = DeepCharcoal)
                    }

                    if (state.approvedMembers.isEmpty() && state.pendingRequests.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxHeight(0.7f), contentAlignment = Alignment.Center) {
                                EliteEmptyTeamState()
                            }
                        }
                    } else {
                        items(state.approvedMembers) { employee ->
                            EliteOperativeRow(
                                employee = employee,
                                onLongClick = {
                                    selectedEmployeeForOptions = employee
                                }
                            )
                        }
                    }
                    
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }

            // Employee Options Sheet
            if (selectedEmployeeForOptions != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedEmployeeForOptions = null },
                    containerColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(
                            text = "Options for ${selectedEmployeeForOptions?.name}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        ListItem(
                            headlineContent = { Text("Edit Role", style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text("Current: ${selectedEmployeeForOptions?.role}") },
                            leadingContent = { Icon(Icons.Default.Edit, null) },
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    showRoleDialog = true
                                })
                            }
                        )
                        
                        ListItem(
                            headlineContent = { Text("Remove from Team", color = ProfessionalError, style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text("Revoke workspace access") },
                            leadingContent = { Icon(Icons.Default.Delete, null, tint = ProfessionalError) },
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    showRemoveDialog = true
                                })
                            }
                        )
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }

            // Warning Removal Dialog
            if (showRemoveDialog) {
                AlertDialog(
                    onDismissRequest = { showRemoveDialog = false },
                    icon = { Icon(Icons.Default.Warning, null, tint = ProfessionalError, modifier = Modifier.size(48.dp)) },
                    title = { Text("Remove Team Member") },
                    text = { Text("Are you sure you want to remove '${selectedEmployeeForOptions?.name}' from your workspace? They will lose access immediately.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedEmployeeForOptions?.let { viewModel.removeEmployee(it.id) }
                                showRemoveDialog = false
                                selectedEmployeeForOptions = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ProfessionalError)
                        ) { Text("Remove") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRemoveDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Role Edit Dialog
            if (showRoleDialog) {
                var roleText by remember { mutableStateOf(selectedEmployeeForOptions?.role ?: "EMPLOYEE") }
                AlertDialog(
                    onDismissRequest = { showRoleDialog = false },
                    title = { Text("Change Role") },
                    text = {
                        OutlinedTextField(
                            value = roleText,
                            onValueChange = { roleText = it },
                            label = { Text("Role / Designation") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            selectedEmployeeForOptions?.let { viewModel.updateRole(it.id, roleText) }
                            showRoleDialog = false
                            selectedEmployeeForOptions = null
                        }) { Text("Update") }
                    }
                )
            }
        }
    }
}

@Composable
fun EliteTeamSectionHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = color))
        Spacer(Modifier.width(12.dp))
        Text("[ ${count.toString().padStart(2, '0')} ]", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = StoneGray)
    }
}

@Composable
fun ElitePendingOperativeRow(employee: Employee, onApprove: () -> Unit, onReject: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).background(ProfessionalWarning, CircleShape))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(employee.name.uppercase(), style = MaterialTheme.typography.labelMedium, color = DeepCharcoal)
                Text(employee.email, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp), color = StoneGray)
            }
            Row {
                IconButton(onClick = onApprove, modifier = Modifier.size(32.dp).background(ProfessionalSuccess.copy(0.1f), RoundedCornerShape(2.dp))) {
                    Icon(Icons.Default.Check, null, tint = ProfessionalSuccess, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onReject, modifier = Modifier.size(32.dp).background(ProfessionalError.copy(0.1f), RoundedCornerShape(2.dp))) {
                    Icon(Icons.Default.Close, null, tint = ProfessionalError, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun EliteOperativeRow(employee: Employee, onLongClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongClick() })
            },
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).background(ProfessionalSuccess, CircleShape))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(employee.name.uppercase(), style = MaterialTheme.typography.labelMedium, color = DeepCharcoal)
                Text("ROLE: ${employee.role.uppercase()}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp), color = StoneGray)
            }
            Text("ACTIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp), color = ProfessionalSuccess)
        }
    }
}

@Composable
fun EliteEmptyTeamState() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Groups, null, modifier = Modifier.size(48.dp), tint = WarmBorder)
        Spacer(Modifier.height(16.dp))
        Text("No team members yet", style = MaterialTheme.typography.labelSmall, color = StoneGray)
    }
}
