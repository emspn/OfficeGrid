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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.core.ui.UiEvent
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
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
                    AdminSettingsActionItem(
                        icon = Icons.Default.CloudUpload,
                        title = "Force Global Sync",
                        description = "Manually refresh all node data",
                        onClick = viewModel::forceGlobalSync
                    )
                    HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    AdminSettingsActionItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Wipe System Cache",
                        description = "Clear temporary operational data",
                        onClick = viewModel::clearLocalCache,
                        destructive = true
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
                        icon = Icons.Default.Groups,
                        title = "Join Requests",
                        description = "Alerts for new team members",
                        checked = settings?.joinRequests ?: true,
                        onCheckedChange = { value -> 
                            viewModel.updateNotificationSetting { it.copy(joinRequests = value) }
                        }
                    )
                    HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    AdminSettingsToggleItem(
                        icon = Icons.AutoMirrored.Filled.Comment,
                        title = "Task Comments",
                        description = "Alerts for status logs and remarks",
                        checked = settings?.remarks ?: true,
                        onCheckedChange = { value -> 
                            viewModel.updateNotificationSetting { it.copy(remarks = value) }
                        }
                    )
                }
            }

            // 3. Security
            AdminSectionHeader(
                title = "SECURITY",
                subtitle = "Data protection"
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column {
                    AdminSettingsActionItem(
                        icon = Icons.Default.Security,
                        title = "Registry Integrity",
                        description = "System-wide security status: SECURE",
                        onClick = { /* Check security */ }
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
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if (enabled) DeepCharcoal else StoneGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (enabled) DeepCharcoal else StoneGray)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = StoneGray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = ProfessionalSuccess, uncheckedTrackColor = WarmBorder)
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
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = if (destructive) ProfessionalError else DeepCharcoal, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (destructive) ProfessionalError else DeepCharcoal)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = StoneGray)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = WarmBorder, modifier = Modifier.size(20.dp))
        }
    }
}
