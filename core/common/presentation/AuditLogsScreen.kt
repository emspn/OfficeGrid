package com.app.officegrid.core.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
                val logs = uiState.data
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
                            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                                Text("SYSTEM_LOGS", style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Black), color = DeepCharcoal)
                                Text("IMMUTABLE_ACTIVITY_HISTORY", style = MaterialTheme.typography.labelSmall, color = StoneGray)
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
                    Text(text = "LOG_FETCH_FAILURE: ${uiState.message}", style = MaterialTheme.typography.labelSmall, color = ProfessionalError)
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
fun ProfessionalEmptyLogsState() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
        Text("NO_LEDGER_ENTRIES", style = MaterialTheme.typography.labelSmall, color = Gray500)
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
