package com.app.officegrid.tasks.presentation.task_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.core.ui.components.EmptyTasksState
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.presentation.components.SwipeableTaskCard
import com.app.officegrid.tasks.presentation.task_list.export.TaskExportHelper
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToCreateTask: () -> Unit,
    onNavigateToEditTask: (String) -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val timelineFilter by viewModel.timelineFilter.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var showTimelineMenu by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    // Sync on screen resume
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.syncTasks()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.app.officegrid.core.ui.UiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is com.app.officegrid.core.ui.UiEvent.Navigate -> {
                    if (event.route.startsWith("task_detail/")) {
                        val taskId = event.route.removePrefix("task_detail/")
                        onNavigateToTaskDetail(taskId)
                    }
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmBackground,
        floatingActionButton = {
            if (currentUser?.role == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = onNavigateToCreateTask,
                    containerColor = DeepCharcoal,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Initialize Task")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(WarmBackground)
        ) {
            when (val uiState = state) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp, modifier = Modifier.size(24.dp))
                    }
                }
                is UiState.Success -> {
                    val tasks = uiState.data

                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.syncTasks() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state = lazyListState,
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AdminSectionHeader(
                                            title = "Missions",
                                            subtitle = "OPERATIONAL_REGISTRY"
                                        )

                                        // ✅ ALIGNED FILTERS AND EXPORT
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // TIMELINE DROPDOWN
                                            Box {
                                                TextButton(onClick = { showTimelineMenu = true }) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp), tint = DeepCharcoal)
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            text = timelineFilter.name,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                                            color = DeepCharcoal
                                                        )
                                                        Icon(Icons.Default.ArrowDropDown, null, tint = DeepCharcoal)
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = showTimelineMenu,
                                                    onDismissRequest = { showTimelineMenu = false },
                                                    modifier = Modifier.background(Color.White)
                                                ) {
                                                    TaskTimelineFilter.entries.forEach { filter ->
                                                        DropdownMenuItem(
                                                            text = { Text(filter.name, style = MaterialTheme.typography.bodySmall) },
                                                            onClick = {
                                                                if (filter == TaskTimelineFilter.CUSTOM) {
                                                                    showDateRangePicker = true
                                                                } else {
                                                                    viewModel.onTimelineFilterSelected(filter)
                                                                }
                                                                showTimelineMenu = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            // 🚀 EXPORT DROPDOWN
                                            if (currentUser?.role == UserRole.ADMIN) {
                                                Box {
                                                    IconButton(onClick = { showExportMenu = true }) {
                                                        Icon(Icons.Default.FileDownload, "Export", tint = DeepCharcoal)
                                                    }
                                                    DropdownMenu(
                                                        expanded = showExportMenu,
                                                        onDismissRequest = { showExportMenu = false },
                                                        modifier = Modifier.background(Color.White)
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Export as PDF", style = MaterialTheme.typography.bodySmall) },
                                                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, null, Modifier.size(18.dp)) },
                                                            onClick = {
                                                                scope.launch {
                                                                    TaskExportHelper.exportToPdf(context, tasks, "OfficeGrid_Report_${System.currentTimeMillis()}")
                                                                }
                                                                showExportMenu = false
                                                            }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Export as CSV", style = MaterialTheme.typography.bodySmall) },
                                                            leadingIcon = { Icon(Icons.Default.TableChart, null, Modifier.size(18.dp)) },
                                                            onClick = {
                                                                scope.launch {
                                                                    TaskExportHelper.exportToCsv(context, tasks, "OfficeGrid_Report_${System.currentTimeMillis()}")
                                                                }
                                                                showExportMenu = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    SearchBar(
                                        query = searchQuery,
                                        onQueryChange = viewModel::onSearchQueryChange,
                                        onSearchClear = viewModel::onSearchClear
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    StatusFilterBar(
                                        selectedStatus = selectedStatus,
                                        onStatusSelected = viewModel::onStatusFilterSelected
                                    )
                                }
                            }

                            if (tasks.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillParentMaxHeight(0.7f), contentAlignment = Alignment.Center) {
                                        EmptyTasksState()
                                    }
                                }
                            } else {
                                items(items = tasks, key = { it.id }) { task ->
                                    SwipeableTaskCard(
                                        task = task,
                                        userRole = currentUser?.role ?: UserRole.EMPLOYEE,
                                        onClick = { onNavigateToTaskDetail(task.id) },
                                        onStatusChange = { newStatus -> viewModel.updateTaskStatus(task.id, newStatus) },
                                        onDelete = { viewModel.deleteTask(task.id) },
                                        onEdit = { onNavigateToEditTask(task.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorState(message = uiState.message, onRetry = { viewModel.syncTasks() })
                }
            }
        }
    }

    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            viewModel.onDateRangeSelected(start, end)
                        }
                        showDateRangePicker = false
                    }
                ) {
                    Text("APPLY_FILTER", fontWeight = FontWeight.Black, color = DeepCharcoal)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text("CANCEL", color = StoneGray)
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.height(400.dp),
                title = { Text("SELECT_TIMELINE_RANGE", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)) },
                headline = { Text("Mission Period", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = DeepCharcoal,
                    headlineContentColor = DeepCharcoal,
                    weekdayContentColor = StoneGray,
                    subheadContentColor = StoneGray,
                    navigationContentColor = DeepCharcoal,
                    yearContentColor = DeepCharcoal,
                    disabledYearContentColor = StoneGray.copy(alpha = 0.3f),
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = DeepCharcoal,
                    dayContentColor = DeepCharcoal,
                    disabledDayContentColor = StoneGray.copy(alpha = 0.3f),
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = DeepCharcoal,
                    todayContentColor = DeepCharcoal,
                    todayDateBorderColor = DeepCharcoal,
                    dayInSelectionRangeContentColor = DeepCharcoal,
                    dayInSelectionRangeContainerColor = DeepCharcoal.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun StatusFilterBar(selectedStatus: TaskStatus?, onStatusSelected: (TaskStatus?) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChipElite("ALL_STATUS", selectedStatus == null) { onStatusSelected(null) } }
        items(TaskStatus.entries.toTypedArray()) { status ->
            FilterChipElite(status.name.replace("_", " "), selectedStatus == status) { onStatusSelected(status) }
        }
    }
}

@Composable
fun FilterChipElite(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) DeepCharcoal else Color.White,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) DeepCharcoal else WarmBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
            color = if (isSelected) Color.White else StoneGray
        )
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onSearchClear: () -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search MISSION_REGISTRY...", style = MaterialTheme.typography.bodySmall) },
        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = WarmBorder,
            focusedBorderColor = DeepCharcoal,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        ),
        textStyle = MaterialTheme.typography.bodySmall,
        singleLine = true
    )
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text(message, color = ProfessionalError, style = MaterialTheme.typography.labelSmall)
        Button(onClick = onRetry) { Text("Retry") }
    }
}
