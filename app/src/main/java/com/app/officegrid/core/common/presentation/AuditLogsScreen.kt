package com.app.officegrid.core.common.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.common.domain.model.AuditLog
import com.app.officegrid.core.common.domain.model.AuditEventType
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.AdminSectionHeader
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
    val selectedType by viewModel.selectedType.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.syncLogs()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val uiState = state) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    }
                }
                is UiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = viewModel::syncLogs,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            item {
                                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        AdminSectionHeader(
                                            title = "REGISTRY_LOGS",
                                            subtitle = "SYSTEM_EVENT_HISTORY"
                                        )
                                        IconButton(onClick = viewModel::syncLogs) {
                                            Icon(Icons.Default.Refresh, null, tint = DeepCharcoal)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Elite Filters
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AuditFilterChip(
                                            label = when(selectedType) {
                                                AuditEventType.CREATE -> "CREATED"
                                                AuditEventType.STATUS_CHANGE -> "UPDATED"
                                                AuditEventType.DELETE -> "DELETED"
                                                else -> "ALL_EVENTS"
                                            },
                                            icon = Icons.Default.FilterList,
                                            modifier = Modifier.weight(1f),
                                            onClick = { /* Implement menu or cycle */ }
                                        )
                                        AuditFilterChip(
                                            label = dateFilter.name,
                                            icon = Icons.Default.Schedule,
                                            modifier = Modifier.weight(1f),
                                            onClick = { /* Implement menu or cycle */ }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = viewModel::onSearchQueryChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Filter logs...", style = MaterialTheme.typography.bodySmall) },
                                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = DeepCharcoal,
                                            unfocusedBorderColor = WarmBorder
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            if (uiState.data.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillParentMaxHeight(0.6f), contentAlignment = Alignment.Center) {
                                        Text("NO_RECORDS_FOUND", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                                    }
                                }
                            } else {
                                items(uiState.data, key = { it.id }) { log ->
                                    EliteAuditLogRow(log)
                                }
                            }
                            
                            item { Spacer(Modifier.height(100.dp)) }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("REGISTRY_ERROR: ${uiState.message}", color = ProfessionalError)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditFilterChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(14.dp), StoneGray)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp), color = DeepCharcoal)
        }
    }
}

@Composable
fun EliteAuditLogRow(log: AuditLog) {
    val accentColor = when (log.eventType) {
        AuditEventType.CREATE -> ProfessionalSuccess
        AuditEventType.DELETE -> ProfessionalError
        else -> DeepCharcoal
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.padding(top = 6.dp).size(6.dp).background(accentColor, CircleShape))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(log.eventType.name, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 8.sp), color = accentColor)
                    Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.createdAt)), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = StoneGray)
                }
                Text(log.title.uppercase(), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black), color = DeepCharcoal)
                Text(log.description, style = MaterialTheme.typography.bodySmall, color = StoneGray)
                Spacer(Modifier.height(8.dp))
                Text("BY: ${log.userEmail}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontFamily = FontFamily.Monospace), color = StoneGray)
            }
        }
    }
}
