package com.app.officegrid.tasks.presentation.task_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.core.ui.components.EmptySearchState
import com.app.officegrid.core.ui.components.EmptyTasksState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.presentation.components.SwipeableTaskCard
import com.app.officegrid.tasks.presentation.task_list.components.SortDialog
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var showSortDialog by remember { mutableStateOf(false) }
    var showTimelineDialog by remember { mutableStateOf(false) }
    var showPdfExportDialog by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var selectedTimeline by remember { mutableStateOf<TimelineFilter>(TimelineFilter.ALL) }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }

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

    // Sort dialog
    if (showSortDialog) {
        SortDialog(
            currentSort = sortOption,
            onSortSelected = viewModel::onSortOptionSelected,
            onDismiss = { showSortDialog = false }
        )
    }

    // Timeline and Export Logic
    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        customStartDate = dateRangePickerState.selectedStartDateMillis
                        customEndDate = dateRangePickerState.selectedEndDateMillis
                        selectedTimeline = TimelineFilter.CUSTOM_RANGE
                        showDateRangePicker = false
                    },
                    enabled = dateRangePickerState.selectedStartDateMillis != null &&
                             dateRangePickerState.selectedEndDateMillis != null,
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalSuccess)
                ) { Text("SELECT") }
            }
        ) { DateRangePicker(state = dateRangePickerState, showModeToggle = false) }
    }

    if (showTimelineDialog) {
        AlertDialog(
            onDismissRequest = { showTimelineDialog = false },
            title = { Text("Timeline Filter", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimelineFilter.entries.forEach { timeline ->
                        Surface(
                            onClick = {
                                if (timeline == TimelineFilter.CUSTOM_RANGE) {
                                    showTimelineDialog = false
                                    showDateRangePicker = true
                                } else {
                                    selectedTimeline = timeline
                                    showTimelineDialog = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (selectedTimeline == timeline) ProfessionalSuccess.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(timeline.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTimelineDialog = false }) { Text("CLOSE") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateTask,
                containerColor = DeepCharcoal,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Initialize Task")
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
                    val filteredTasksByTimeline = filterTasksByTimeline(
                        uiState.data,
                        selectedTimeline,
                        customStartDate,
                        customEndDate
                    )

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
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        AdminSectionHeader(
                                            title = "Tasks",
                                            subtitle = "ACTIVE_OPERATIONAL_UNITS"
                                        )

                                        IconButton(onClick = { showSortDialog = true }) {
                                            Icon(Icons.Default.FilterList, "Sort", tint = DeepCharcoal)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            onClick = { showTimelineDialog = true },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            color = Color.White,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                                        ) {
                                            Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp), StoneGray)
                                                Spacer(Modifier.width(8.dp))
                                                Text(selectedTimeline.name, style = MaterialTheme.typography.labelSmall, color = DeepCharcoal)
                                            }
                                        }
                                        Button(
                                            onClick = { showPdfExportDialog = true },
                                            modifier = Modifier.height(40.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ProfessionalSuccess),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp)
                                        ) {
                                            Icon(Icons.Default.FileDownload, null, Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("EXPORT", style = MaterialTheme.typography.labelSmall)
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

                            val pendingCount = uiState.data.count { it.status == TaskStatus.PENDING_COMPLETION }
                            if (pendingCount > 0 && selectedStatus != TaskStatus.PENDING_COMPLETION) {
                                item {
                                    Surface(
                                        onClick = { viewModel.onStatusFilterSelected(TaskStatus.PENDING_COMPLETION) },
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        color = Color(0xFFFF9800),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Notifications, null, tint = Color.White)
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text("APPROVALS PENDING", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                                                Text("$pendingCount tasks await verification", color = Color.White.copy(0.8f), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            if (filteredTasksByTimeline.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillParentMaxHeight(0.7f), contentAlignment = Alignment.Center) {
                                        EmptyTasksState()
                                    }
                                }
                            } else {
                                items(items = filteredTasksByTimeline, key = { it.id }) { task ->
                                    SwipeableTaskCard(
                                        task = task,
                                        onClick = { viewModel.onTaskClick(task.id) },
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
}

@Composable
fun StatusFilterBar(selectedStatus: TaskStatus?, onStatusSelected: (TaskStatus?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChipElite("ALL", selectedStatus == null) { onStatusSelected(null) } }
        items(TaskStatus.entries.toTypedArray()) { status ->
            FilterChipElite(status.name, selectedStatus == status) { onStatusSelected(status) }
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
        placeholder = { Text("Search by task title or node...", style = MaterialTheme.typography.bodySmall) },
        leadingIcon = { Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp)) },
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

private fun filterTasksByTimeline(tasks: List<Task>, timeline: TimelineFilter, customStart: Long?, customEnd: Long?): List<Task> {
    val startOfToday = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
    return when (timeline) {
        TimelineFilter.ALL -> tasks
        TimelineFilter.TODAY -> tasks.filter { it.dueDate >= startOfToday && it.dueDate <= startOfToday + 86400000 }
        else -> tasks
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text(message, color = ProfessionalError, style = MaterialTheme.typography.labelSmall)
        Button(onClick = onRetry) { Text("Retry") }
    }
}
