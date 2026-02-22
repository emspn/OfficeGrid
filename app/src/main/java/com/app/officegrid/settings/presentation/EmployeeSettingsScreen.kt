package com.app.officegrid.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.ui.UiEvent
import com.app.officegrid.employee.presentation.common.EmployeeTopBar
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = WarmBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            title = "Task Assignments",
                            description = "Alerts for new assignments",
                            checked = settings?.taskAssigned ?: true,
                            onCheckedChange = { value -> 
                                viewModel.updateNotificationSetting { it.copy(taskAssigned = value) }
                            }
                        )
                        HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        EmployeeSettingsToggleItem(
                            icon = Icons.AutoMirrored.Filled.Comment,
                            title = "Remarks & Updates",
                            description = "Alerts for task discussion",
                            checked = settings?.remarks ?: true,
                            onCheckedChange = { value -> 
                                viewModel.updateNotificationSetting { it.copy(remarks = value) }
                            }
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
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.CloudUpload,
                            title = "Synchronize Now",
                            description = "Refresh local task node registry",
                            onClick = viewModel::forceGlobalSync
                        )
                        HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        EmployeeSettingsActionItem(
                            icon = Icons.Default.DeleteForever,
                            title = "Clear Local Cache",
                            description = "Wipe temporary offline storage",
                            onClick = viewModel::clearLocalCache,
                            destructive = true
                        )
                    }
                }
            }

            // 3. About
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
                            description = "1.0.0 (Stable Production Build)",
                            onClick = { /* Info */ }
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
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = if (enabled) DeepCharcoal.copy(alpha = 0.05f) else StoneGray.copy(alpha = 0.3f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = if (enabled) DeepCharcoal else StoneGray, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (enabled) DeepCharcoal else StoneGray)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = if (enabled) StoneGray else StoneGray.copy(alpha = 0.6f))
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = if (destructive) ProfessionalError.copy(alpha = 0.1f) else DeepCharcoal.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = if (destructive) ProfessionalError else DeepCharcoal, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (destructive) ProfessionalError else DeepCharcoal)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = StoneGray)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = StoneGray, modifier = Modifier.size(20.dp))
        }
    }
}
