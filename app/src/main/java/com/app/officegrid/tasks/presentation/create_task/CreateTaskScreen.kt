package com.app.officegrid.tasks.presentation.create_task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarToday
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
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.launch

@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateTaskViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val priority by viewModel.priority.collectAsState()
    val assignedTo by viewModel.assignedTo.collectAsState()
    val dueDate by viewModel.dueDate.collectAsState()
    val employees by viewModel.employees.collectAsState()

    var priorityExpanded by remember { mutableStateOf(false) }
    var employeeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTemplateSelector by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> {
                    // Launch snackbar in a separate coroutine so it doesn't block navigation
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                is UiEvent.Navigate -> {
                    // Navigate back immediately
                    onNavigateBack()
                }
                else -> Unit
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        viewModel.onDueDateChange(selectedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("CONFIRM", style = MaterialTheme.typography.labelSmall, color = DeepCharcoal)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("CANCEL", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = WarmBackground
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = WarmBackground,
                    selectedDayContainerColor = DeepCharcoal,
                    todayDateBorderColor = DeepCharcoal
                )
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmBackground
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = WarmBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // Reclaimed Header Space with Back Navigation
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gray900)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TASK_INITIALIZATION", style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Black), color = DeepCharcoal)
                        Text("UNIT_ALLOCATION_INTERFACE", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                    }

                    // Use Template Button
                    OutlinedButton(
                        onClick = { showTemplateSelector = true },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DeepCharcoal
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp)
                    ) {
                        Icon(
                            Icons.Default.Apps,
                            contentDescription = "Templates",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Templates",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Template Selector Dialog
                if (showTemplateSelector) {
                    com.app.officegrid.tasks.presentation.templates.TaskTemplateSelectionScreen(
                        onTemplateSelected = { template ->
                            showTemplateSelector = false
                            // Create tasks from template
                            viewModel.createTasksFromTemplate(template)
                        },
                        onDismiss = { showTemplateSelector = false }
                    )
                }

                // Technical Ledger Section: Core Identity
                EliteFormSection(label = "CORE_IDENTIFIER") {
                    EliteTextField(
                        value = title,
                        onValueChange = viewModel::onTitleChange,
                        placeholder = "ASSIGNMENT_TITLE",
                        singleLine = true,
                        enabled = !state.isLoading
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    EliteTextField(
                        value = description,
                        onValueChange = viewModel::onDescriptionChange,
                        placeholder = "OPERATIONAL_SPECIFICATIONS",
                        minLines = 4,
                        enabled = !state.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Technical Ledger Section: Deployment Metadata
                EliteFormSection(label = "DEPLOYMENT_METADATA") {
                    // Priority Selector
                    EliteDropdown(
                        label = "PRIORITY_LEVEL",
                        value = priority.name,
                        icon = Icons.Default.Flag,
                        iconColor = when(priority) {
                            TaskPriority.HIGH -> ProfessionalError
                            TaskPriority.MEDIUM -> ProfessionalWarning
                            else -> DeepCharcoal
                        },
                        expanded = priorityExpanded,
                        onExpandedChange = { priorityExpanded = it },
                        enabled = !state.isLoading
                    ) {
                        TaskPriority.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        p.name, 
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                                    ) 
                                },
                                onClick = {
                                    viewModel.onPriorityChange(p)
                                    priorityExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    // Employee Selector
                    val selectedEmployee = employees.find { it.id == assignedTo }
                    EliteDropdown(
                        label = "ASSIGNED_NODE",
                        value = selectedEmployee?.name ?: "SELECT_OPERATIVE",
                        icon = Icons.Default.Person,
                        expanded = employeeExpanded,
                        onExpandedChange = { employeeExpanded = it },
                        enabled = !state.isLoading
                    ) {
                        if (employees.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("NO_ACTIVE_OPERATIVES", style = MaterialTheme.typography.labelSmall) },
                                onClick = { employeeExpanded = false }
                            )
                        } else {
                            employees.forEach { emp ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(emp.name.uppercase(), style = MaterialTheme.typography.labelMedium)
                                            Text(emp.email, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = StoneGray)
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

                    Spacer(modifier = Modifier.height(1.dp))

                    // Due Date Selector
                    val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }
                    val formattedDate = remember(dueDate) { dateFormat.format(java.util.Date(dueDate)) }

                    Surface(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        enabled = !state.isLoading
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = DeepCharcoal)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DUE_DATE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = StoneGray)
                                Text(formattedDate.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Action Button
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp, modifier = Modifier.size(24.dp))
                    }
                } else {
                    Surface(
                        onClick = viewModel::createTask,
                        modifier = Modifier.fillMaxWidth(),
                        color = DeepCharcoal,
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            text = "INITIALIZE_TASK",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }

                state.error?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "CONFIGURATION_ERROR: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalError,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun EliteFormSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MutedSlate
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun EliteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall, color = WarmBorder) },
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = DeepCharcoal,
            focusedTextColor = DeepCharcoal,
            unfocusedTextColor = DeepCharcoal
        ),
        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
        shape = RoundedCornerShape(0.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EliteDropdown(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color = DeepCharcoal,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) onExpandedChange(it) }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = iconColor)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = StoneGray)
                    Text(value.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace))
                }
                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp), tint = StoneGray)
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(Color.White),
            content = content
        )
    }
}
