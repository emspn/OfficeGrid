package com.app.officegrid.core.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsScreen(
    viewModel: AuditLogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<AuditEventType?>(null) }

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
                val filteredLogs = remember(allLogs, searchQuery, selectedFilter) {
                    allLogs.filter { log ->
                        val matchesSearch = searchQuery.isEmpty() ||
                            log.title.contains(searchQuery, ignoreCase = true) ||
                            log.description.contains(searchQuery, ignoreCase = true) ||
                            log.userEmail.contains(searchQuery, ignoreCase = true)

                        val matchesFilter = selectedFilter == null || log.eventType == selectedFilter

                        matchesSearch && matchesFilter
                    }
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
                            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("AUDIT_LOGS", style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Black), color = DeepCharcoal)
                                        Text("${filteredLogs.size} EVENTS", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                                    }

                                    IconButton(onClick = { /* Export logs */ }) {
                                        Icon(Icons.Default.FileDownload, "Export", tint = DeepCharcoal)
                                    }
                                }
                            }
                        }

                        // Search Bar
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search logs...", style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, "Clear", modifier = Modifier.size(20.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = DeepCharcoal,
                                    unfocusedBorderColor = WarmBorder
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        // Filter Chips
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedFilter == null,
                                        onClick = { selectedFilter = null },
                                        label = { Text("ALL", style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DeepCharcoal,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }

                                AuditEventType.entries.forEach { eventType ->
                                    item {
                                        FilterChip(
                                            selected = selectedFilter == eventType,
                                            onClick = { selectedFilter = if (selectedFilter == eventType) null else eventType },
                                            label = { Text(eventType.name, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = when (eventType) {
                                                    AuditEventType.CREATE -> ProfessionalSuccess
                                                    AuditEventType.DELETE -> ProfessionalError
                                                    AuditEventType.STATUS_CHANGE -> ProfessionalWarning
                                                    else -> DeepCharcoal
                                                },
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }

                        if (filteredLogs.isEmpty()) {
                            item {
                                ProfessionalEmptyLogsState(
                                    hasFilters = searchQuery.isNotEmpty() || selectedFilter != null
                                )
                            }
                        } else {
                            items(filteredLogs, key = { it.id }) { log ->
                                EliteAuditLogRow(log)
                            }
                        }
                    }
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Failed to load audit logs", style = MaterialTheme.typography.bodyLarge, color = ProfessionalError)
                        Spacer(Modifier.height(8.dp))
                        Text(text = uiState.message, style = MaterialTheme.typography.bodySmall, color = StoneGray)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::syncLogs) {
                            Text("Retry")
                        }
                    }
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
                    text = "AUTH_REF: ${log.userEmail}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                    color = StoneGray
                )
            }
        }
    }
}

@Composable
fun ProfessionalEmptyLogsState(hasFilters: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(64.dp),
                color = StoneGray.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "📋",
                        style = MaterialTheme.typography.displaySmall,
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                if (hasFilters) "NO_MATCHING_LOGS" else "NO_AUDIT_LOGS_YET",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepCharcoal
            )

            Text(
                if (hasFilters) "Try adjusting your filters" else "Activity will appear here",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGray
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
