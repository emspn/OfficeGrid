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
                isRefreshing = false,
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
                                    subtitle = "Manage active workspace operatives"
                                )
                                IconButton(onClick = viewModel::syncTeam) {
                                    Icon(Icons.Default.Refresh, null, tint = DeepCharcoal)
                                }
                            }
                        }
                    }

                    if (state.pendingRequests.isNotEmpty()) {
                        item {
                            EliteTeamSectionHeader(title = "Pending Authorization", count = state.pendingRequests.size, color = ProfessionalWarning)
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

                    item {
                        EliteTeamSectionHeader(title = "Active Operatives", count = state.approvedMembers.size, color = DeepCharcoal)
                    }

                    if (state.approvedMembers.isEmpty() && state.pendingRequests.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxHeight(0.7f), contentAlignment = Alignment.Center) {
                                EliteEmptyTeamState()
                            }
                        }
                    } else {
                        items(state.approvedMembers) { employee ->
                            val isSelf = employee.id == state.currentUserId
                            EliteOperativeRow(
                                employee = employee,
                                isSelf = isSelf,
                                onLongClick = {
                                    if (!isSelf) {
                                        selectedEmployeeForOptions = employee
                                    }
                                }
                            )
                        }
                    }
                    
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }

            if (selectedEmployeeForOptions != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedEmployeeForOptions = null },
                    containerColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(
                            text = "OPERATIVE_SPECIFICATIONS: ${selectedEmployeeForOptions?.name}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        ListItem(
                            headlineContent = { Text("Update Authorization Level", style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text("Current Role: ${selectedEmployeeForOptions?.role}") },
                            leadingContent = { Icon(Icons.Default.Security, null) },
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    showRoleDialog = true
                                })
                            }
                        )
                        
                        ListItem(
                            headlineContent = { Text("Terminate Connection", color = ProfessionalError, style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text("Revoke node access immediately") },
                            leadingContent = { Icon(Icons.Default.PowerSettingsNew, null, tint = ProfessionalError) },
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

            if (showRemoveDialog) {
                AlertDialog(
                    onDismissRequest = { showRemoveDialog = false },
                    icon = { Icon(Icons.Default.Warning, null, tint = ProfessionalError, modifier = Modifier.size(48.dp)) },
                    title = { Text("CONFIRM TERMINATION") },
                    text = { Text("Are you sure you want to disconnect operative '${selectedEmployeeForOptions?.name}'? All active mission access will be revoked.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedEmployeeForOptions?.let { viewModel.removeEmployee(it.id) }
                                showRemoveDialog = false
                                selectedEmployeeForOptions = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ProfessionalError)
                        ) { Text("TERMINATE") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRemoveDialog = false }) { Text("CANCEL") }
                    }
                )
            }

            if (showRoleDialog) {
                var roleText by remember { mutableStateOf(selectedEmployeeForOptions?.role ?: "OPERATIVE") }
                AlertDialog(
                    onDismissRequest = { showRoleDialog = false },
                    title = { Text("UPDATE ROLE") },
                    text = {
                        OutlinedTextField(
                            value = roleText,
                            onValueChange = { roleText = it },
                            label = { Text("Authorization Level") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            selectedEmployeeForOptions?.let { viewModel.updateRole(it.id, roleText) }
                            showRoleDialog = false
                            selectedEmployeeForOptions = null
                        }) { Text("UPDATE") }
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
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = color))
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
fun EliteOperativeRow(employee: Employee, isSelf: Boolean, onLongClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { if (!isSelf) onLongClick() })
            },
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).background(if (isSelf) DeepCharcoal else ProfessionalSuccess, CircleShape))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (isSelf) "${employee.name.uppercase()} (YOU)" else employee.name.uppercase(), 
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelf) FontWeight.ExtraBold else FontWeight.Medium), 
                    color = DeepCharcoal
                )
                Text("LEVEL: ${employee.role.uppercase()}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp), color = StoneGray)
            }
            if (isSelf) {
                Text("CORE_ADMIN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp), color = DeepCharcoal)
            } else {
                Text("AUTHORIZED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp), color = ProfessionalSuccess)
            }
        }
    }
}

@Composable
fun EliteEmptyTeamState() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Groups, null, modifier = Modifier.size(48.dp), tint = WarmBorder)
        Spacer(Modifier.height(16.dp))
        Text("No operatives in node", style = MaterialTheme.typography.labelSmall, color = StoneGray)
    }
}
