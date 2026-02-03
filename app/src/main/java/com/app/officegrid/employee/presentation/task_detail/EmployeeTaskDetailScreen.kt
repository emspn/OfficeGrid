package com.app.officegrid.employee.presentation.task_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.employee.presentation.common.*
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.presentation.task_detail.TaskDetailViewModel
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeTaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val taskState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            Surface(color = Color.White, border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = DeepCharcoal)
                    }
                    Text(
                        "Task Details",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                        color = DeepCharcoal,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(WarmBackground)) {
            when (val state = taskState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp)
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = ProfessionalError, style = MaterialTheme.typography.labelSmall)
                    }
                }
                is UiState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val task = (state as UiState.Success<Task>).data
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Header Badge
                        Surface(
                            color = DeepCharcoal,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Task ID: ${task.id.take(8).uppercase()}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        // Title Section
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = task.title.uppercase(),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = DeepCharcoal
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                EmployeeStatusBadge(status = task.status)
                                Text(
                                    text = "Priority: ${task.priority.name}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = StoneGray
                                )
                            }
                        }

                        // Description Box
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                            "Description",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                color = StoneGray
                            )
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                            ) {
                                Text(
                                    text = if (task.description.isBlank()) "No description provided." else task.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = DeepCharcoal,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        // Due Date
                        if (task.dueDate > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = StoneGray)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Due: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.dueDate))}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                    color = if (task.dueDate < System.currentTimeMillis() && task.status != TaskStatus.DONE) ProfessionalError else DeepCharcoal
                                )
                            }
                        }

                        // ═══════════════════════════════════════════
                        // REMARKS SECTION
                        // ═══════════════════════════════════════════
                        Spacer(Modifier.height(16.dp))

                        RemarksSection(
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState
                        )

                        Spacer(Modifier.height(24.dp))

                        // Unified Action Node
                        EliteOperativeActionArea(
                            status = task.status,
                            onUpdate = { newStatus ->
                                scope.launch {
                                    viewModel.updateStatus(newStatus)
                                    snackbarHostState.showSnackbar("Status updated to ${newStatus.name.replace("_", " ")}")
                                }
                            }
                        )
                        
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EliteOperativeActionArea(
    status: TaskStatus,
    onUpdate: (TaskStatus) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (status) {
            TaskStatus.TODO -> {
                Button(
                    onClick = { onUpdate(TaskStatus.IN_PROGRESS) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalSuccess),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Start Working", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                }
            }
            TaskStatus.IN_PROGRESS -> {
                Button(
                    onClick = { onUpdate(TaskStatus.PENDING_COMPLETION) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Submit for Review", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                }
                
                TextButton(
                    onClick = { onUpdate(TaskStatus.TODO) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Move Back to To Do", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                }
            }
            TaskStatus.PENDING_COMPLETION -> {
                // ELITE IMPROVED COMPLETION REQUEST CARD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2196F3))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = Color(0xFF2196F3).copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            text = "Awaiting Approval",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color(0xFF1976D2)
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            text = "System has broadcasted your completion request. Awaiting administrator authentication.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            textAlign = TextAlign.Center,
                            color = Gray700
                        )
                        
                        Spacer(Modifier.height(20.dp))
                        
                        // Small pulsing dots indicator
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(3) { index ->
                                Box(
                                    Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF2196F3).copy(alpha = 0.3f + (index * 0.2f)), CircleShape)
                                )
                            }
                        }
                    }
                }
            }
            TaskStatus.DONE -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ProfessionalSuccess.copy(0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ProfessionalSuccess.copy(0.3f))
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.CheckCircle, null, tint = ProfessionalSuccess, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Task Completed", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = ProfessionalSuccess)
                    }
                }
            }
        }
    }
}

/**
 * Remarks/Comments Section
 */
@Composable
private fun RemarksSection(
    viewModel: TaskDetailViewModel,
    snackbarHostState: SnackbarHostState
) {
    val remarks by viewModel.remarks.collectAsState(initial = emptyList())
    val remarkMessage by viewModel.remarkMessage.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Text(
            "Comments",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            color = StoneGray
        )

        // Remarks List
        if (remarks.isEmpty()) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Text(
                    text = "No activity logged yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    remarks.forEachIndexed { index, remark ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Avatar
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = DeepCharcoal.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = remark.createdBy.take(1).uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DeepCharcoal
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = remark.createdBy,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepCharcoal
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = remark.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray700
                                )
                            }
                        }
                        if (index < remarks.lastIndex) {
                            HorizontalDivider(color = WarmBorder, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // Add Remark Input
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = remarkMessage,
                    onValueChange = { viewModel.onRemarkMessageChange(it) },
                    placeholder = { Text("Add a comment...", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f),
                    enabled = !isUpdating,
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        scope.launch {
                            viewModel.addRemark()
                            snackbarHostState.showSnackbar("Comment added")
                        }
                    },
                    enabled = remarkMessage.isNotBlank() && !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = DeepCharcoal
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (remarkMessage.isNotBlank()) DeepCharcoal else StoneGray
                        )
                    }
                }
            }
        }
    }
}

