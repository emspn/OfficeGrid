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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshTaskAndRemarks()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
            title = { Text("Delete Task", style = MaterialTheme.typography.labelLarge, color = DeepCharcoal) },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.", style = MaterialTheme.typography.bodySmall, color = StoneGray) },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.deleteTask()
                    showDeleteDialog = false
                }) {
                    Text("Delete", style = MaterialTheme.typography.labelSmall, color = ProfessionalError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", style = MaterialTheme.typography.labelSmall, color = DeepCharcoal)
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
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
                    currentUser = currentUser,
                    onNavigateBack = onNavigateBack,
                    onNavigateToEdit = onNavigateToEdit,
                    onShowDelete = { showDeleteDialog = true },
                    onRemarkChange = viewModel::onRemarkMessageChange,
                    onAddRemark = viewModel::addRemark,
                    onUpdateStatus = viewModel::updateStatus
                )
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error loading task: ${uiState.message}", style = MaterialTheme.typography.labelSmall, color = ProfessionalError)
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
    currentUser: com.app.officegrid.auth.domain.model.User?,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onShowDelete: () -> Unit,
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gray900)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Task Details", style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Black), color = DeepCharcoal)
                Text("Operational Metadata", style = MaterialTheme.typography.labelSmall, color = StoneGray)
            }
            if (currentUser?.role == UserRole.ADMIN) {
                IconButton(onClick = { onNavigateToEdit(task.id) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DeepCharcoal, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onShowDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ProfessionalError, modifier = Modifier.size(20.dp))
                }
            }
        }

        // 1. Technical Task Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                StatusBadgeElite(status = task.status)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = task.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = DeepCharcoal)
                Text(text = "UNIT_ID: ${task.id.take(8).uppercase()}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = StoneGray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Metadata Grid
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

        // 🚀 NEW: TWITTER-STYLE AD SLOT (Strategic Placement)
        TaskDetailAdSlot()

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Description Section
        EliteDetailSection(label = "OPERATIONAL_SPECIFICATIONS") {
            Surface(
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = task.description.ifBlank { "No detailed specifications provided." },
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray700,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Workflow Actions
        EliteDetailSection(label = "WORKFLOW_STATE_MANAGEMENT") {
            if (task.status == TaskStatus.PENDING_COMPLETION && currentUser?.role == UserRole.ADMIN) {
                AdminApprovalBanner(isUpdating, onUpdateStatus)
            }

            // Status Picker UI
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
                                Text("Update Registry Status", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color.White))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 5. Activity Logs (Remarks)
        EliteDetailSection(label = "COMMUNICATION_LOGS") {
            if (remarks.isEmpty()) {
                Text("No entries in communication log.", style = MaterialTheme.typography.labelSmall, color = StoneGray)
            } else {
                remarks.forEach { remark ->
                    ProfessionalRemarkRow(remark)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Remark Input
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = remarkMessage,
                onValueChange = onRemarkChange,
                placeholder = { Text("Enter log entry...", style = MaterialTheme.typography.labelSmall, color = WarmBorder) },
                modifier = Modifier.weight(1f),
                enabled = !isUpdating,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = DeepCharcoal,
                    unfocusedIndicatorColor = WarmBorder
                ),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = onAddRemark,
                enabled = !isUpdating && remarkMessage.isNotBlank(),
                modifier = Modifier.background(DeepCharcoal, RoundedCornerShape(2.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp), tint = Color.White)
            }
        }
    }
}

@Composable
private fun TaskDetailAdSlot() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SPONSORED_CONTENT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.sp), color = StoneGray)
                Spacer(Modifier.height(4.dp))
                Text("Premium Ad Space (Twitter-Style)", style = MaterialTheme.typography.bodySmall, color = WarmBorder)
            }
            
            // "Ad" marker
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                color = Gray100,
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("Ad", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp, color = StoneGray)
            }
        }
    }
}

@Composable
private fun AdminApprovalBanner(isUpdating: Boolean, onUpdateStatus: (TaskStatus) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        color = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF9800))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚡ REVIEW REQUIRED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = Color(0xFFFF6F00))
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onUpdateStatus(TaskStatus.DONE) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalSuccess),
                    enabled = !isUpdating
                ) { Text("APPROVE", style = MaterialTheme.typography.labelSmall) }
                OutlinedButton(
                    onClick = { onUpdateStatus(TaskStatus.IN_PROGRESS) },
                    modifier = Modifier.weight(1f),
                    enabled = !isUpdating
                ) { Text("REJECT", style = MaterialTheme.typography.labelSmall, color = ProfessionalError) }
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
        TaskStatus.TODO -> StoneGray to "To Do"
        TaskStatus.IN_PROGRESS -> ProfessionalWarning to "In Progress"
        TaskStatus.PENDING_COMPLETION -> Color(0xFF2196F3) to "Pending Review"
        TaskStatus.DONE -> ProfessionalSuccess to "Completed"
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
            Text(text = value.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace), color = accentColor)
        }
    }
}

@Composable
fun StatusSelectButtonElite(status: TaskStatus, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
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
                Text(text = remark.createdBy.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp), color = DeepCharcoal)
                Text(text = formatDetailDate(remark.createdAt), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp), color = StoneGray)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = remark.message, style = MaterialTheme.typography.bodySmall, color = Gray700, lineHeight = 16.sp)
        }
    }
}

fun formatDetailDate(timestamp: Long): String {
    if (timestamp == 0L) return "0000-00-00"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
