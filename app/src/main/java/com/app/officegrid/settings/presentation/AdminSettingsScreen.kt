package com.app.officegrid.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    onNavigateBack: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var autoSync by remember { mutableStateOf(true) }
    var realtimeUpdates by remember { mutableStateOf(true) }
    var auditLogging by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SYSTEM_CONFIGURATION",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = DeepCharcoal,
                    navigationIconContentColor = DeepCharcoal
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Connectivity & Data
            AdminSectionHeader(
                title = "CORE_CONNECTIVITY",
                subtitle = "Active data synchronization parameters"
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column {
                    AdminSettingsToggleItem(
                        icon = Icons.Default.Sync,
                        title = "Background Sync",
                        description = "Keep local registry updated automatically",
                        checked = autoSync,
                        onCheckedChange = { autoSync = it }
                    )
                    HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    AdminSettingsToggleItem(
                        icon = Icons.Default.Bolt,
                        title = "Real-time Node Updates",
                        description = "Instant WebSocket synchronization",
                        checked = realtimeUpdates,
                        onCheckedChange = { realtimeUpdates = it }
                    )
                    HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    AdminSettingsActionItem(
                        icon = Icons.Default.CloudUpload,
                        title = "Force Global Sync",
                        description = "Manually refresh all node data",
                        onClick = { /* Sync Logic */ }
                    )
                }
            }

            // 2. Notification Preferences
            AdminSectionHeader(
                title = "ALERT_SYSTEM",
                subtitle = "Manage operational notifications"
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column {
                    AdminSettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications",
                        description = "System-wide alerts for task updates",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    AdminSettingsToggleItem(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Critical Alert Sound",
                        description = "Enable audio for priority events",
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        enabled = notificationsEnabled
                    )
                }
            }

            // 3. Security & Safety
            AdminSectionHeader(
                title = "SECURITY_&_VALIDATION",
                subtitle = "Data protection and log integrity"
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column {
                    AdminSettingsToggleItem(
                        icon = Icons.Default.History,
                        title = "Automated Audit Logging",
                        description = "Trace all administrative interactions",
                        checked = auditLogging,
                        onCheckedChange = { auditLogging = it }
                    )
                    HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    AdminSettingsActionItem(
                        icon = Icons.Default.Lock,
                        title = "Change Master Password",
                        description = "Update your primary access key",
                        onClick = { /* Logic */ }
                    )
                }
            }

            // 4. Maintenance
            AdminSectionHeader(
                title = "SYSTEM_MAINTENANCE"
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column {
                    AdminSettingsActionItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Clear Local Cache",
                        description = "Wipe temporary offline storage",
                        onClick = { /* Logic */ },
                        destructive = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AdminSettingsToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) DeepCharcoal else StoneGray,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) DeepCharcoal else StoneGray
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = StoneGray
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = ProfessionalSuccess,
                uncheckedTrackColor = WarmBorder
            )
        )
    }
}

@Composable
private fun AdminSettingsActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (destructive) ProfessionalError else DeepCharcoal,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (destructive) ProfessionalError else DeepCharcoal
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = WarmBorder,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
