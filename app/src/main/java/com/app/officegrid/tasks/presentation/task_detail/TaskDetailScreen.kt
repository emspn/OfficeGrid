package com.app.officegrid.tasks.presentation.task_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.core.ui.UiEvent
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskRemark
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val remarks by viewModel.remarks.collectAsState(initial = emptyList())
    val remarkMessage by viewModel.remarkMessage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(initial = null)
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Navigate -> { if (event.route == "back") onNavigateBack() }
                else -> Unit
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(2.dp),
            title = { Text("PURGE_CONFIRMATION", style = MaterialTheme.typography.labelLarge, color = DeepCharcoal) },
            text = { Text("Warning: Permanent removal of operational unit. Continue?", style = MaterialTheme.typography.bodySmall, color = StoneGray) },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.deleteTask()
                    showDeleteDialog = false
                }) {
                    Text("CONFIRM_PURGE", style = MaterialTheme.typography.labelSmall, color = ProfessionalError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ABORT", style = MaterialTheme.typography.labelSmall, color = DeepCharcoal)
                }
            }
        )
    }

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("UNIT_SPECIFICATIONS", style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.sp), color = Gray900)
                        Text("SYSTEM_OPERATIONAL_OVERVIEW", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gray900)
                    }
                },
                actions = {
                    if (currentUser?.role == UserRole.ADMIN) {
                        IconButton(onClick = { (state as? UiState.Success)?.data?.id?.let(onNavigateToEdit) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DeepCharcoal, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ProfessionalError, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmBackground),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = WarmBackground
        ) {
            when (val uiState = state) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp, modifier = Modifier.size(24.dp))
                    }
                }
                is UiState.Success -> {
                    TaskDetailContent(
                        task = uiState.data,
                        remarks = remarks,
                        remarkMessage = remarkMessage,
                        isUpdating = isUpdating,
                        onRemarkChange = viewModel::onRemarkMessageChange,
                        onAddRemark = viewModel::addRemark,
                        onUpdateStatus = viewModel::updateStatus
                    )
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "UNIT_FETCH_FAILURE: ${uiState.message}", style = MaterialTheme.typography.labelSmall, color = ProfessionalError)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskDetailContent(
    task: Task,
    remarks: List<TaskRemark>,
    remarkMessage: String,
    isUpdating: Boolean,
    onRemarkChange: (String) -> Unit,
    onAddRemark: () -> Unit,
    onUpdateStatus: (TaskStatus) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(task.status) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Technical Header Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                StatusBadgeElite(status = task.status)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = DeepCharcoal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "UNIT_ID: ${task.id.uppercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = StoneGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Metadata Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetadataCardElite(
                modifier = Modifier.weight(1f),
                label = "PRIORITY_LEVEL",
                value = task.priority.name,
                accentColor = when(task.priority) {
                    TaskPriority.HIGH -> ProfessionalError
                    TaskPriority.MEDIUM -> ProfessionalWarning
                    else -> DeepCharcoal
                }
            )
            MetadataCardElite(
                modifier = Modifier.weight(1f),
                label = "DEADLINE_VAL",
                value = formatDetailDate(task.dueDate),
                accentColor = StoneGray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Description Section
        EliteDetailSection(label = "OPERATIONAL_DESCRIPTION") {
            Surface(
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = task.description.ifBlank { "NO_SPECIFICATIONS_PROVIDED" },
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray700,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Status Control
        EliteDetailSection(label = "WORKFLOW_STATE_MANAGEMENT") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskStatus.entries.forEach { status ->
                            StatusSelectButtonElite(
                                status = status,
                                isSelected = selectedStatus == status,
                                onClick = { if (!isUpdating) selectedStatus = status },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        onClick = { onUpdateStatus(selectedStatus) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating && selectedStatus != task.status,
                        color = if (!isUpdating && selectedStatus != task.status) DeepCharcoal else WarmBorder,
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                            if (isUpdating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 1.dp)
                            } else {
                                Text(
                                    "COMMIT_STATUS_TRANSITION", 
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color.White)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Activity Log
        EliteDetailSection(label = "DEPLOYMENT_TIMELINE") {
            if (remarks.isEmpty()) {
                Text("NO_TIMELINE_ENTRIES", style = MaterialTheme.typography.labelSmall, color = StoneGray)
            } else {
                remarks.forEach { remark ->
                    ProfessionalRemarkRow(remark)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Remark Input
        EliteDetailSection(label = "LOG_UPDATE_ENTRY") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = remarkMessage,
                    onValueChange = onRemarkChange,
                    placeholder = { Text("ENTER_LOG_METADATA...", style = MaterialTheme.typography.labelSmall, color = WarmBorder) },
                    modifier = Modifier.weight(1f),
                    enabled = !isUpdating,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = DeepCharcoal,
                        unfocusedIndicatorColor = WarmBorder,
                        cursorColor = DeepCharcoal,
                        focusedTextColor = DeepCharcoal,
                        unfocusedTextColor = DeepCharcoal
                    ),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onAddRemark,
                    enabled = !isUpdating && remarkMessage.isNotBlank(),
                    modifier = Modifier.background(DeepCharcoal, RoundedCornerShape(2.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(18.dp), tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun EliteDetailSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MutedSlate))
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun StatusBadgeElite(status: TaskStatus) {
    val (dotColor, label) = when (status) {
        TaskStatus.TODO -> StoneGray to "STATE_PENDING"
        TaskStatus.IN_PROGRESS -> ProfessionalWarning to "STATE_ACTIVE"
        TaskStatus.DONE -> ProfessionalSuccess to "STATE_TERMINATED"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = DeepCharcoal)
    }
}

@Composable
fun MetadataCardElite(modifier: Modifier, label: String, value: String, accentColor: Color) {
    Surface(
        modifier = modifier,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = StoneGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value.uppercase(), 
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace), 
                color = accentColor
            )
        }
    }
}

@Composable
fun StatusSelectButtonElite(status: TaskStatus, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(0.dp),
        color = if (isSelected) DeepCharcoal else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = status.name,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                color = if (isSelected) Color.White else StoneGray
            )
        }
    }
}

@Composable
fun ProfessionalRemarkRow(remark: TaskRemark) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "LOG_NODE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp), color = DeepCharcoal)
                Text(text = formatDetailDate(remark.createdAt), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp), color = StoneGray)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = remark.message,
                style = MaterialTheme.typography.bodySmall,
                color = Gray700,
                lineHeight = 16.sp
            )
        }
    }
}

fun formatDetailDate(timestamp: Long): String {
    if (timestamp == 0L) return "0000-00-00"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
