package com.app.officegrid.core.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
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
import com.app.officegrid.core.common.domain.model.AuditLog
import com.app.officegrid.core.common.domain.model.AuditEventType
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AuditEventFilter {
    ALL_TYPES,
    CREATE,
    COMPLETED,  // Maps to STATUS_CHANGE (includes task completion/approval events)
    DELETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsScreen(
    viewModel: AuditLogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showEventFilterDialog by remember { mutableStateOf(false) }
    var selectedEventFilter by remember { mutableStateOf<AuditEventFilter>(AuditEventFilter.ALL_TYPES) }

    // Event Filter Dialog
    if (showEventFilterDialog) {
        AlertDialog(
            onDismissRequest = { showEventFilterDialog = false },
            title = {
                Text(
                    "Select Event Type",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuditEventFilter.values().forEach { filter ->
                        Surface(
                            onClick = {
                                selectedEventFilter = filter
                                showEventFilterDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (selectedEventFilter == filter) ProfessionalSuccess.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                when (filter) {
                                    AuditEventFilter.ALL_TYPES -> "All Types"
                                    AuditEventFilter.CREATE -> "Create"
                                    AuditEventFilter.COMPLETED -> "Completed"
                                    AuditEventFilter.DELETED -> "Deleted"
                                },
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (selectedEventFilter == filter) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = DeepCharcoal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEventFilterDialog = false }) {
                    Text("CLOSE")
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
                val allLogs = uiState.data
                // Apply event type filter
                val logs = when (selectedEventFilter) {
                    AuditEventFilter.ALL_TYPES -> allLogs
                    AuditEventFilter.CREATE -> allLogs.filter { it.eventType == AuditEventType.CREATE }
                    AuditEventFilter.COMPLETED -> allLogs.filter { it.eventType == AuditEventType.STATUS_CHANGE }  // Approved/completed tasks
                    AuditEventFilter.DELETED -> allLogs.filter { it.eventType == AuditEventType.DELETE }
                }

                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = viewModel::syncLogs,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                Text(
                                    "Activity",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = DeepCharcoal
                                )
                                Text(
                                    "Recent activity in your workspace",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StoneGray
                                )
                            }
                        }

                        item {
                            // Event Type Filter Dropdown
                            Surface(
                                onClick = { showEventFilterDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp),
                                shadowElevation = 2.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.FilterList,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = DeepCharcoal
                                        )
                                        Text(
                                            when (selectedEventFilter) {
                                                AuditEventFilter.ALL_TYPES -> "All Types"
                                                AuditEventFilter.CREATE -> "Create"
                                                AuditEventFilter.COMPLETED -> "Completed"
                                                AuditEventFilter.DELETED -> "Deleted"
                                            },
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = DeepCharcoal
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = DeepCharcoal
                                    )
                                }
                            }
                        }

                        if (logs.isEmpty()) {
                            item { ProfessionalEmptyLogsState() }
                        } else {
                            items(logs, key = { it.id }) { log ->
                                EliteAuditLogRow(log)
                            }
                        }
                    }
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error loading activity: ${uiState.message}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalError
                    )
                }
            }
        }
    }
}

@Composable
fun EliteAuditLogRow(log: AuditLog) {
    val accentColor = remember(log.eventType) {
        when (log.eventType) {
            AuditEventType.CREATE -> ProfessionalSuccess
            AuditEventType.DELETE -> ProfessionalError
            AuditEventType.STATUS_CHANGE -> ProfessionalWarning
            else -> Gray900
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(6.dp)
                    .background(accentColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.eventType.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = accentColor
                    )
                    Text(
                        text = formatTimestamp(log.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = Gray500
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color = Gray900
                )
                
                Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray700,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "By: ${log.userEmail}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = StoneGray
                )
            }
        }
    }
}

@Composable
fun ProfessionalEmptyLogsState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No activity yet",
            style = MaterialTheme.typography.labelSmall,
            color = Gray500
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

