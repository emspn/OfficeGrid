package com.app.officegrid.core.common.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.common.AppNotification
import com.app.officegrid.core.common.NotificationType
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.ui.theme.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTask: ((String) -> Unit)? = null,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "NOTIFICATIONS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = DeepCharcoal
                        )
                        if (unreadCount > 0) {
                            Text(
                                "$unreadCount unread",
                                style = MaterialTheme.typography.labelSmall,
                                color = StoneGray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepCharcoal
                        )
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text(
                                "Mark all read",
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepCharcoal
                            )
                        }
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "More options", tint = DeepCharcoal)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear all notifications") },
                            onClick = {
                                viewModel.clearAllNotifications()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmBackground)
            )
        }
    ) { paddingValues ->
        when (val uiState = state) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 2.dp)
                }
            }

            is UiState.Success -> {
                if (uiState.data.isEmpty()) {
                    EmptyNotificationState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.data,
                            key = { it.id }
                        ) { notification ->
                            SwipeableNotificationItem(
                                notification = notification,
                                onDelete = { viewModel.deleteNotification(notification.id) },
                                onClick = {
                                    viewModel.markAsRead(notification.id)
                                    // Navigate to task if relatedId exists
                                    notification.relatedId?.let { taskId ->
                                        onNavigateToTask?.invoke(taskId)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = ProfessionalError
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Failed to load notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepCharcoal
                        )
                        Text(
                            uiState.message,
                            style = MaterialTheme.typography.labelSmall,
                            color = StoneGray
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNotificationItem(
    notification: AppNotification,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> ProfessionalError
                    else -> Color.Transparent
                },
                label = "swipe_color"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        content = {
            NotificationCard(notification = notification, onClick = onClick)
        },
        enableDismissFromStartToEnd = false
    )
}

@Composable
fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val (icon, iconColor) = getNotificationIconAndColor(notification.type)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (notification.isRead) Color.White else AccentGray,
        border = androidx.compose.foundation.BorderStroke(
            width = if (notification.isRead) 0.5.dp else 1.dp,
            color = if (notification.isRead) WarmBorder else iconColor.copy(alpha = 0.3f)
        ),
        tonalElevation = if (notification.isRead) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold
                        ),
                        color = if (notification.isRead) MutedSlate else DeepCharcoal,
                        modifier = Modifier.weight(1f)
                    )

                    // Unread indicator
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(iconColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray,
                    lineHeight = 18.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatRelativeTime(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = StoneGray
                    )

                    if (notification.relatedId != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            color = StoneGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap to view",
                            style = MaterialTheme.typography.labelSmall,
                            color = iconColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AccentGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = StoneGray
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "All caught up!",
            style = MaterialTheme.typography.titleMedium,
            color = DeepCharcoal
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "No notifications at the moment",
            style = MaterialTheme.typography.bodySmall,
            color = StoneGray
        )
    }
}

/**
 * Get icon and color based on notification type
 */
private fun getNotificationIconAndColor(type: NotificationType): Pair<ImageVector, Color> {
    return when (type) {
        NotificationType.TASK_ASSIGNED -> Icons.Default.AddTask to DeepCharcoal
        NotificationType.TASK_UPDATED -> Icons.Default.EditNote to MutedSlate
        NotificationType.TASK_COMPLETED -> Icons.Default.CheckCircle to ProfessionalSuccess
        NotificationType.NEW_REMARK -> Icons.Default.ChatBubble to MutedSlate
        NotificationType.TASK_OVERDUE -> Icons.Default.Warning to ProfessionalError
        NotificationType.JOIN_REQUEST -> Icons.Default.PersonAdd to ProfessionalWarning
        NotificationType.JOIN_APPROVED -> Icons.Default.HowToReg to ProfessionalSuccess
        NotificationType.JOIN_REJECTED -> Icons.Default.PersonOff to ProfessionalError
        NotificationType.SYSTEM -> Icons.Default.Info to StoneGray
    }
}

/**
 * Format timestamp to relative time (e.g., "2m ago", "1h ago", "Yesterday")
 */
private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            "${minutes}m ago"
        }
        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            "${hours}h ago"
        }
        diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
        diff < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            "${days}d ago"
        }
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}

