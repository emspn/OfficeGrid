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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.employee.presentation.common.EmployeeTopBar
import com.app.officegrid.ui.theme.*

/**
 * 👤 EMPLOYEE SETTINGS SCREEN
 * Simple personal settings for employees
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeSettingsScreen(
    onNavigateBack: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var autoSync by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            EmployeeTopBar(
                title = "SETTINGS",
                onBackClick = onNavigateBack
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
            // 1. Notifications Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmployeeSettingsSectionHeader("NOTIFICATIONS")
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column {
                        EmployeeSettingsToggleItem(
                            icon = Icons.Default.Notifications,
                            title = "Push Notifications",
                            description = "Get alerts for new task assignments",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                        HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        EmployeeSettingsToggleItem(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            title = "Notification Sound",
                            description = "Play audio for incoming alerts",
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it },
                            enabled = notificationsEnabled
                        )
                    }
                }
            }

            // 2. Data & Synchronization
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmployeeSettingsSectionHeader("DATA & SYNC")
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column {
                        EmployeeSettingsToggleItem(
                            icon = Icons.Default.Sync,
                            title = "Auto Sync",
                            description = "Automatic background synchronization",
                            checked = autoSync,
                            onCheckedChange = { autoSync = it }
                        )
                        HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.CloudUpload,
                            title = "Force Sync Now",
                            description = "Manually sync your tasks",
                            onClick = { /* Force sync */ }
                        )
                    }
                }
            }

            // 3. Appearance
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmployeeSettingsSectionHeader("APPEARANCE")
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column {
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.Palette,
                            title = "Theme",
                            description = "System default",
                            onClick = { /* Change theme */ }
                        )
                        HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.Language,
                            title = "Language",
                            description = "English",
                            onClick = { /* Change language */ }
                        )
                    }
                }
            }

            // 4. Account
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmployeeSettingsSectionHeader("ACCOUNT")
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column {
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.Lock,
                            title = "Change Password",
                            description = "Update your account password",
                            onClick = { /* Change password */ }
                        )
                        HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.Security,
                            title = "Privacy & Security",
                            description = "Manage your privacy settings",
                            onClick = { /* Privacy settings */ }
                        )
                    }
                }
            }

            // 5. Storage
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmployeeSettingsSectionHeader("STORAGE")
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column {
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.Storage,
                            title = "Storage Usage",
                            description = "12.5 MB used",
                            onClick = { /* Show storage details */ }
                        )
                        HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.DeleteForever,
                            title = "Clear Cache",
                            description = "Free up space by clearing cache",
                            onClick = { /* Clear cache */ },
                            destructive = true
                        )
                    }
                }
            }

            // 6. About
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmployeeSettingsSectionHeader("ABOUT")
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column {
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.Info,
                            title = "App Version",
                            description = "1.0.0 (Build 2026.02.01)",
                            onClick = { /* Show version info */ }
                        )
                        HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.Help,
                            title = "Help & Support",
                            description = "Get help and contact support",
                            onClick = { /* Help center */ }
                        )
                        HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.Description,
                            title = "Terms & Privacy",
                            description = "View policies and terms",
                            onClick = { /* Terms */ }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EmployeeSettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        ),
        color = StoneGray
    )
}

@Composable
private fun EmployeeSettingsToggleItem(
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
        Surface(
            modifier = Modifier.size(40.dp),
            color = if (enabled) DeepCharcoal.copy(alpha = 0.05f) else StoneGray.copy(alpha = 0.3f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) DeepCharcoal else StoneGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) DeepCharcoal else StoneGray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) StoneGray else StoneGray.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ProfessionalSuccess,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = StoneGray.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun EmployeeSettingsActionItem(
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
            Surface(
                modifier = Modifier.size(40.dp),
                color = if (destructive) ProfessionalError.copy(alpha = 0.1f) else DeepCharcoal.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (destructive) ProfessionalError else DeepCharcoal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (destructive) ProfessionalError else DeepCharcoal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = StoneGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
