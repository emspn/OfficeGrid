package com.app.officegrid.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.ui.theme.*

@Suppress("unused", "UNUSED_PARAMETER")
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    var autoSync by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    "SYSTEM_CONFIGURATION",
                    style = MaterialTheme.typography.titleLarge.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = DeepCharcoal
                )
                Text(
                    "PREFERENCES_&_OPTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGray
                )
            }

            // Notifications Section
            SettingsSectionHeader("NOTIFICATIONS")
            Spacer(Modifier.height(12.dp))

            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Enable Notifications",
                description = "Receive alerts for tasks and updates",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            Spacer(Modifier.height(1.dp))

            SettingsToggleItem(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Sound",
                description = "Play sound for notifications",
                checked = soundEnabled,
                onCheckedChange = { soundEnabled = it },
                enabled = notificationsEnabled
            )

            Spacer(Modifier.height(1.dp))

            SettingsToggleItem(
                icon = Icons.Default.Vibration,
                title = "Vibration",
                description = "Vibrate on notifications",
                checked = vibrationEnabled,
                onCheckedChange = { vibrationEnabled = it },
                enabled = notificationsEnabled
            )

            Spacer(Modifier.height(32.dp))

            // Appearance Section
            SettingsSectionHeader("APPEARANCE")
            Spacer(Modifier.height(12.dp))

            SettingsToggleItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                description = "Use dark theme (Coming Soon)",
                checked = darkMode,
                onCheckedChange = { darkMode = it },
                enabled = false
            )

            Spacer(Modifier.height(32.dp))

            // Data & Sync Section
            SettingsSectionHeader("DATA_&_SYNC")
            Spacer(Modifier.height(12.dp))

            SettingsToggleItem(
                icon = Icons.Default.Sync,
                title = "Auto Sync",
                description = "Automatically sync data in background",
                checked = autoSync,
                onCheckedChange = { autoSync = it }
            )

            Spacer(Modifier.height(1.dp))

            SettingsActionItem(
                icon = Icons.Default.CloudUpload,
                title = "Sync Now",
                description = "Manually sync all data",
                onClick = { /* Trigger sync */ }
            )

            Spacer(Modifier.height(1.dp))

            SettingsActionItem(
                icon = Icons.Default.DeleteForever,
                title = "Clear Cache",
                description = "Free up storage space",
                onClick = { /* Clear cache */ }
            )

            Spacer(Modifier.height(32.dp))

            // About Section
            SettingsSectionHeader("ABOUT")
            Spacer(Modifier.height(12.dp))

            SettingsInfoItem(
                icon = Icons.Default.Info,
                title = "Version",
                value = "1.0.0"
            )

            Spacer(Modifier.height(1.dp))

            SettingsActionItem(
                icon = Icons.Default.Description,
                title = "Privacy Policy",
                description = "View our privacy policy",
                onClick = { /* Open privacy policy */ }
            )

            Spacer(Modifier.height(1.dp))

            SettingsActionItem(
                icon = Icons.Default.Gavel,
                title = "Terms of Service",
                description = "View terms and conditions",
                onClick = { /* Open terms */ }
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        ),
        color = MutedSlate
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
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
                modifier = Modifier.size(20.dp),
                tint = if (enabled) DeepCharcoal else StoneGray
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (enabled) DeepCharcoal else StoneGray
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = StoneGray
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = DeepCharcoal,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = WarmBorder
                )
            )
        }
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
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
                modifier = Modifier.size(20.dp),
                tint = DeepCharcoal
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = DeepCharcoal
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = StoneGray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = StoneGray
            )
        }
    }
}

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
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
                modifier = Modifier.size(20.dp),
                tint = DeepCharcoal
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = DeepCharcoal,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = StoneGray
            )
        }
    }
}
