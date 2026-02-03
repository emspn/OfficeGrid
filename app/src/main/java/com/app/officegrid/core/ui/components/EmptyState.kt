package com.app.officegrid.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.ui.theme.*

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Surface(
            modifier = Modifier.size(80.dp),
            color = AccentGray,
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = StoneGray
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = DeepCharcoal,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = StoneGray,
            textAlign = TextAlign.Center
        )

        // Action button
        if (actionLabel != null && onActionClick != null) {
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepCharcoal
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyTasksState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Default.TaskAlt,
        title = "No Tasks",
        description = "No tasks found. Create your first task to get started.",
        modifier = modifier
    )
}

@Composable
fun EmptySearchState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Default.SearchOff,
        title = "No Results Found",
        description = "Try adjusting your search or filters to find what you're looking for.",
        modifier = modifier
    )
}

@Composable
fun EmptyTeamState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Default.Groups,
        title = "No Team Members",
        description = "No team members found. Invite employees to join your workspace.",
        modifier = modifier
    )
}

@Composable
fun EmptyNotificationsState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Default.NotificationsNone,
        title = "No Notifications",
        description = "You're all caught up! No new notifications at this time.",
        modifier = modifier
    )
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.ErrorOutline,
        title = "Something Went Wrong",
        description = message,
        actionLabel = if (onRetry != null) "Retry" else null,
        onActionClick = onRetry,
        modifier = modifier
    )
}

@Composable
fun NoInternetState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.CloudOff,
        title = "No Connection",
        description = "No internet connection. Please check your network and try again.",
        actionLabel = "Retry",
        onActionClick = onRetry,
        modifier = modifier
    )
}

