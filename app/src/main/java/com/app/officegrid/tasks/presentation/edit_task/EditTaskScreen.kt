package com.app.officegrid.tasks.presentation.edit_task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
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
import com.app.officegrid.core.ui.UiEvent
import com.app.officegrid.core.ui.AdminTopBar
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditTaskViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val priority by viewModel.priority.collectAsState()
    val assignedTo by viewModel.assignedTo.collectAsState()
    val employees by viewModel.employees.collectAsState()

    var priorityExpanded by remember { mutableStateOf(false) }
    var employeeExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // ⚡ NEW: Refresh employees on EVERY screen resume
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                android.util.Log.d("EditTaskScreen", "⚡ Screen resumed - refreshing employees")
                viewModel.refreshEmployees()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is UiEvent.Navigate) {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            AdminTopBar(
                title = "UPDATE_SPECIFICATIONS",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AdminSectionHeader(
                    title = "OPERATIONAL_RECONFIGURATION",
                    subtitle = "Modify core identifiers and deployment node"
                )

                // 1. Core Identifiers Card
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = viewModel::onTitleChange,
                            placeholder = { Text("Assignment Title") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            singleLine = true,
                            enabled = !state.isLoading
                        )
                        HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = viewModel::onDescriptionChange,
                            placeholder = { Text("Detailed Operational Specifications...") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            enabled = !state.isLoading
                        )
                    }
                }

                // 2. Deployment Metadata
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DEPLOYMENT_METADATA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = StoneGray)
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                    ) {
                        Column {
                            // Priority
                            AdminFormRow(
                                label = "PRIORITY_LEVEL",
                                value = priority.name,
                                icon = Icons.Default.Flag,
                                onClick = { priorityExpanded = true }
                            )
                            
                            HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                            
                            // Operative
                            val selectedEmployee = employees.find { it.id == assignedTo }
                            AdminFormRow(
                                label = "ASSIGNED_NODE",
                                value = selectedEmployee?.name ?: "Select Team Member",
                                icon = Icons.Default.Person,
                                onClick = { employeeExpanded = true }
                            )
                        }
                    }
                }

                // Commit Button
                Button(
                    onClick = viewModel::updateTask,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("COMMIT_CHANGES", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp))
                    }
                }

                state.error?.let {
                    Text(
                        text = "CONFIGURATION_ERROR: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalError,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(Modifier.height(32.dp))
            }

            // --- Menus ---

            // Priority Dropdown
            DropdownMenu(
                expanded = priorityExpanded,
                onDismissRequest = { priorityExpanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                TaskPriority.entries.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.name, style = MaterialTheme.typography.labelMedium) },
                        onClick = {
                            viewModel.onPriorityChange(p)
                            priorityExpanded = false
                        }
                    )
                }
            }

            // Operative Dropdown
            DropdownMenu(
                expanded = employeeExpanded,
                onDismissRequest = { employeeExpanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                if (employees.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No approved team members", style = MaterialTheme.typography.labelSmall) },
                        onClick = { employeeExpanded = false }
                    )
                } else {
                    employees.forEach { emp ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(emp.name.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(emp.email, style = MaterialTheme.typography.labelSmall, color = StoneGray)
                                }
                            },
                            onClick = {
                                viewModel.onAssignedToChange(emp.id)
                                employeeExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminFormRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = DeepCharcoal)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = StoneGray)
                Text(value.uppercase(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
            }
            Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(18.dp), tint = StoneGray)
        }
    }
}
