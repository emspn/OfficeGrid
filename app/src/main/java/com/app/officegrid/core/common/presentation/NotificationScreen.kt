package com.app.officegrid.core.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.common.AppNotification
import com.app.officegrid.core.common.NotificationType
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SYSTEM_INBOX", style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.sp), color = Gray900)
                        Text("ACTIVE_COMMUNICATION_STREAM", style = MaterialTheme.typography.labelSmall, color = StoneGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gray900)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text("MARK_ALL_READ", style = MaterialTheme.typography.labelSmall, color = Gray700)
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
                        CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp)
                    }
                }
                is UiState.Success -> {
                    if (uiState.data.isEmpty()) {
                        EmptyNotificationState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            items(uiState.data, key = { it.id }) { notification ->
                                EliteNotificationRow(notification) {
                                    viewModel.markAsRead(notification.id)
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "STREAM_FAILURE: ${uiState.message}", style = MaterialTheme.typography.labelSmall, color = ProfessionalError)
                    }
                }
            }
        }
    }
}

@Composable
fun EliteNotificationRow(notification: AppNotification, onClick: () -> Unit) {
    val accentColor = when (notification.type) {
        NotificationType.TASK_ASSIGNED -> DeepCharcoal
        NotificationType.TASK_UPDATED -> Gray700
        NotificationType.TASK_COMPLETED -> ProfessionalSuccess
        NotificationType.NEW_REMARK -> MutedSlate
        NotificationType.TASK_OVERDUE -> ProfessionalError
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = if (notification.isRead) Color.White else Gray50,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread Pulse
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(6.dp)
                        .background(accentColor, CircleShape)
                )
            } else {
                Spacer(modifier = Modifier.size(6.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.type.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                        color = accentColor
                    )
                    Text(
                        text = formatTime(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                        color = Gray500
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color = if (notification.isRead) Gray700 else Gray900,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold
                )
                
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.NotificationsNone, 
            null, 
            modifier = Modifier.size(48.dp), 
            tint = WarmBorder
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("INBOX_VACANT", style = MaterialTheme.typography.labelSmall, color = Gray500)
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
