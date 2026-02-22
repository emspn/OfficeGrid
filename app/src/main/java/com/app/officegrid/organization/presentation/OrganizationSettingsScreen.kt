package com.app.officegrid.organization.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.core.ui.AdminTopBar
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
            AdminTopBar(
                title = "HUB_SPECIFICATIONS",
                onBackClick = onNavigateBack
            )
        },
        containerColor = WarmBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = orgState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp, modifier = Modifier.size(24.dp))
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CONNECTION_ERROR: ${state.message}", color = ProfessionalError, style = MaterialTheme.typography.labelSmall)
                    }
                }
                is UiState.Success -> {
                    OrganizationContent(data = state.data)
                }
            }
        }
    }
}

@Composable
private fun OrganizationContent(
    data: OrganizationData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Core Hub Section
        AdminSectionHeader(
            title = "CORE_IDENTITY",
            subtitle = "Verified hub identifiers"
        )

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
        ) {
            Column {
                OrganizationTextField(
                    label = "NODE_NAME",
                    value = data.companyName,
                    icon = Icons.Default.Business,
                    enabled = false
                )
                HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                OrganizationTextField(
                    label = "HUB_IDENTIFIER",
                    value = data.companyId,
                    icon = Icons.Default.Badge,
                    enabled = false
                )
                HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(horizontal = 16.dp))
                OrganizationTextField(
                    label = "SECTOR",
                    value = data.industry,
                    icon = Icons.Default.Category,
                    enabled = false
                )
            }
        }

        // Plan & Limits
        AdminSectionHeader(
            title = "RESOURCE_ALLOCATION",
            subtitle = "Operational limits and deployment status"
        )

        PlanInfoCard(
            planName = data.planName,
            maxEmployees = data.maxEmployees,
            currentEmployees = data.currentEmployees,
            storageUsed = data.storageUsed,
            storageLimit = data.storageLimit
        )

        // Danger Zone
        AdminSectionHeader(
            title = "TERMINATION_ZONE",
            subtitle = "Irreversible organizational actions"
        )

        DangerActionCard(
            title = "DECOMMISSION_HUB",
            description = "Permanently purge all data and node access. THIS_ACTION_IS_FINAL.",
            actionLabel = "PURGE",
            onAction = { /* Show confirmation dialog */ }
        )

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun OrganizationTextField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = DeepCharcoal)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = StoneGray)
            Text(
                value.uppercase(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (enabled) DeepCharcoal else Gray500
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
        shape = RoundedCornerShape(12.dp),
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
                        planName.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                        color = DeepCharcoal
                    )
                    Text(
                        "STATUS: ACTIVE_OPERATIONAL_TIER",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalSuccess
                    )
                }

                Icon(Icons.Default.Verified, contentDescription = null, tint = ProfessionalSuccess, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Operatives Usage
            PlanMetricRow(
                label = "OPERATIVE_REGISTRY_SIZE",
                value = "$currentEmployees / $maxEmployees",
                progress = if (maxEmployees > 0) currentEmployees.toFloat() / maxEmployees.toFloat() else 0f,
                icon = Icons.Default.Groups
            )

            Spacer(Modifier.height(16.dp))

            // Storage Usage
            PlanMetricRow(
                label = "DATA_STORAGE",
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
                Icon(icon, null, modifier = Modifier.size(14.dp), tint = StoneGray)
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = DeepCharcoal
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = StoneGray
            )
        }

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = when {
                progress >= 0.8f -> ProfessionalError
                progress >= 0.6f -> ProfessionalWarning
                else -> DeepCharcoal
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
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProfessionalError.copy(alpha = 0.5f))
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
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = ProfessionalError
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = StoneGray
                )
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = ProfessionalError),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = Color.White)
            }
        }
    }
}
