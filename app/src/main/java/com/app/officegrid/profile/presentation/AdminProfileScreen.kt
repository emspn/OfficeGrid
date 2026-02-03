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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.officegrid.core.ui.AdminSectionHeader
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.ui.theme.*

@Composable
fun AdminProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToOrganization: () -> Unit = {},
    onNavigateToAudit: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        when (val state = profileState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 2.dp)
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error loading profile", color = ProfessionalError, style = MaterialTheme.typography.labelSmall)
                }
            }
            is UiState.Success -> {
                state.data?.let { profile ->
                    AdminProfileContent(
                        profile = profile,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToOrganization = onNavigateToOrganization,
                        onNavigateToAudit = onNavigateToAudit,
                        onLogout = { viewModel.logout() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminProfileContent(
    profile: ProfileData,
    onNavigateToSettings: () -> Unit,
    onNavigateToOrganization: () -> Unit,
    onNavigateToAudit: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DeepCharcoal
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar (Redirected from Email via UI Gravatar/Initials pattern)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://www.gravatar.com/avatar/${profile.email.trim().lowercase().md5()}?s=200&d=identicon")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.fullName.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color.White
                )

                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Workspace Identification
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VpnKey, null, tint = ProfessionalWarning, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "WORKSPACE_ID: ${profile.companyId.take(12)}...",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            AdminSectionHeader(
                title = "ORGANIZATION_SPECIFICATIONS"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdminMenuItem(
                icon = Icons.Default.Business,
                title = "Organization Settings",
                subtitle = "Core identifiers and allocation",
                onClick = onNavigateToOrganization
            )

            AdminMenuItem(
                icon = Icons.Default.Terminal,
                title = "Audit Logs",
                subtitle = "System event registry",
                onClick = onNavigateToAudit
            )

            Spacer(modifier = Modifier.height(32.dp))

            AdminSectionHeader(
                title = "SYSTEM_CONFIGURATION"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdminMenuItem(
                icon = Icons.Default.Settings,
                title = "App Settings",
                subtitle = "UI preferences and cache management",
                onClick = onNavigateToSettings
            )

            AdminMenuItem(
                icon = Icons.Default.Security,
                title = "Security & Privacy",
                subtitle = "Session keys and data protection",
                onClick = { /* Security Logic */ }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Logout, null, tint = ProfessionalError, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("Log Out", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = ProfessionalError)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AdminMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = DeepCharcoal, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), color = DeepCharcoal)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StoneGray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = WarmBorder)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// Extension for MD5 (simplified for profile image redirect)
fun String.md5(): String {
    return java.security.MessageDigest.getInstance("MD5")
        .digest(this.toByteArray())
        .joinToString("") { "%02x".format(it) }
}
