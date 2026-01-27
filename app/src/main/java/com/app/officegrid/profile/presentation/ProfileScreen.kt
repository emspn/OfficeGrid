package com.app.officegrid.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.common.presentation.NotificationSettingsViewModel
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.ui.theme.*

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.state.collectAsState()
    val settingsState by settingsViewModel.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "ACCOUNT_OVERVIEW",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = Gray900
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Unified Profile Console
            when (val uiState = profileState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Gray900, strokeWidth = 1.dp)
                    }
                }
                is UiState.Success -> {
                    uiState.data?.let { profile ->
                        EliteProfileConsole(profile)
                    }
                }
                is UiState.Error -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ProfessionalError.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ProfessionalError.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            text = "SYNC_ERROR: ${uiState.message}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfessionalError,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 2. Notification Control Section
            Text(
                text = "NOTIFICATION_PREFERENCES",
                style = MaterialTheme.typography.labelMedium,
                color = MutedSlate,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (val uiState = settingsState) {
                        is UiState.Loading -> {
                            CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp, modifier = Modifier.size(20.dp))
                        }
                        is UiState.Success -> {
                            uiState.data?.let { settings ->
                                ConsoleToggle("Assignment Alerts", settings.taskAssigned) { 
                                    settingsViewModel.updateSettings(settings.copy(taskAssigned = it)) 
                                }
                                HorizontalDivider(color = Gray50, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                                ConsoleToggle("Status Updates", settings.taskUpdated) { 
                                    settingsViewModel.updateSettings(settings.copy(taskUpdated = it)) 
                                }
                                HorizontalDivider(color = Gray50, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                                ConsoleToggle("Critical Deadlines", settings.taskOverdue) { 
                                    settingsViewModel.updateSettings(settings.copy(taskOverdue = it)) 
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 3. System Termination
            Button(
                onClick = { profileViewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
            ) {
                Icon(Icons.Default.PowerSettingsNew, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("SIGN_OUT", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "SESSION_ID: ${System.currentTimeMillis().toString(36).uppercase()}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = Gray200,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun EliteProfileConsole(profile: ProfileData) {
    Column {
        // Hero Identity Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DeepCharcoal,
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = profile.fullName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(profile.fullName.uppercase(), style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(ProfessionalSuccess, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = ProfessionalSuccess)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Technical Data Points
        Text("USER_IDENTITY", style = MaterialTheme.typography.labelMedium, color = MutedSlate, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ConsoleInfoRow("ROLE", profile.role)
                Spacer(Modifier.height(20.dp))
                ConsoleInfoRow("EMAIL", profile.email, isMonospace = true)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Workspace Distribution
        Text("ORGANISATION_DETAILS", style = MaterialTheme.typography.labelMedium, color = MutedSlate, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ConsoleInfoRow("NAME", profile.companyName ?: "NOT_SPECIFIED")
                Spacer(Modifier.height(20.dp))
                ConsoleInfoRow("ID", profile.companyId, isMonospace = true)
            }
        }
    }
}

@Composable
fun ConsoleInfoRow(label: String, value: String, isMonospace: Boolean = false) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = StoneGray)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = DeepCharcoal
        )
    }
}

@Composable
fun ConsoleToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Gray700)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DeepCharcoal,
                uncheckedThumbColor = Gray500,
                uncheckedTrackColor = WarmBorder,
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.scale(0.8f) 
        )
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)
