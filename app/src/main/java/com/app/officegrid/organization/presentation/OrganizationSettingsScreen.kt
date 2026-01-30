package com.app.officegrid.organization.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: OrganizationViewModel = hiltViewModel()
) {
    val orgState by viewModel.organizationData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ORGANIZATION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = DeepCharcoal
                )
            )
        },
        containerColor = WarmBackground
    ) { padding ->
        when (val state = orgState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DeepCharcoal)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${state.message}", color = ProfessionalError)
                }
            }
            is UiState.Success -> {
                OrganizationContent(data = state.data, padding = padding)
            }
        }
    }
}

@Composable
private fun OrganizationContent(
    data: OrganizationData,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Company Info Section
        SectionHeader("COMPANY_INFORMATION")
        Spacer(Modifier.height(16.dp))

        OrganizationTextField(
            label = "Company Name",
            value = data.companyName,
            onValueChange = { },
            icon = Icons.Default.Business,
            enabled = false,
            helperText = "Company name set during registration"
        )

        Spacer(Modifier.height(12.dp))

        OrganizationTextField(
            label = "Company ID",
            value = data.companyId,
            onValueChange = { },
            icon = Icons.Default.Badge,
            enabled = false,
            helperText = "This is your unique organization identifier"
        )

        Spacer(Modifier.height(12.dp))

        OrganizationTextField(
            label = "Industry",
            value = data.industry,
            onValueChange = { },
            icon = Icons.Default.Category,
            enabled = false,
            helperText = "Industry classification"
        )

        Spacer(Modifier.height(32.dp))

        // Plan & Limits
        SectionHeader("SUBSCRIPTION_&_LIMITS")
        Spacer(Modifier.height(16.dp))

        PlanInfoCard(
            planName = data.planName,
            maxEmployees = data.maxEmployees,
            currentEmployees = data.currentEmployees,
            storageUsed = data.storageUsed,
            storageLimit = data.storageLimit
        )

        Spacer(Modifier.height(32.dp))

        // Danger Zone
        SectionHeader("DANGER_ZONE", color = ProfessionalError)
        Spacer(Modifier.height(16.dp))

        DangerActionCard(
            title = "Delete Organization",
            description = "Permanently delete this organization and all associated data. This action cannot be undone.",
            actionLabel = "DELETE",
            onAction = { /* Show confirmation dialog */ }
        )

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SectionHeader(title: String, color: Color = MutedSlate) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        ),
        color = color
    )
}

@Composable
private fun OrganizationTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    helperText: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = AccentGray,
                focusedBorderColor = DeepCharcoal,
                unfocusedBorderColor = WarmBorder
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
        )

        if (helperText != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = StoneGray,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun PlanInfoCard(
    planName: String,
    maxEmployees: Int,
    currentEmployees: Int,
    storageUsed: String,
    storageLimit: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        planName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal
                    )
                    Text(
                        "Active subscription",
                        style = MaterialTheme.typography.labelSmall,
                        color = StoneGray
                    )
                }

                Surface(
                    color = ProfessionalSuccess.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "ACTIVE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ProfessionalSuccess
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Employees Usage
            PlanMetricRow(
                label = "Employees",
                value = "$currentEmployees / $maxEmployees",
                progress = if (maxEmployees > 0) currentEmployees.toFloat() / maxEmployees.toFloat() else 0f,
                icon = Icons.Default.People
            )

            Spacer(Modifier.height(12.dp))

            // Storage Usage
            PlanMetricRow(
                label = "Storage",
                value = "$storageUsed / $storageLimit",
                progress = 0.24f,
                icon = Icons.Default.Storage
            )
        }
    }
}

@Composable
private fun PlanMetricRow(
    label: String,
    value: String,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = StoneGray)
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = DeepCharcoal
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = StoneGray
            )
        }

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = when {
                progress >= 0.8f -> ProfessionalError
                progress >= 0.6f -> ProfessionalWarning
                else -> ProfessionalSuccess
            },
            trackColor = WarmBorder,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun DangerActionCard(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ProfessionalError.copy(alpha = 0.05f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProfessionalError.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = ProfessionalError,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ProfessionalError
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = StoneGray
                )
            }

            Spacer(Modifier.width(12.dp))

            OutlinedButton(
                onClick = onAction,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ProfessionalError
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProfessionalError)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
