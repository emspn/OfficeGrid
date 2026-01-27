package com.app.officegrid.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupRoleScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCompanySignup: () -> Unit,
    onNavigateToEmployeeSignup: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Minimalist Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Back",
                        tint = DeepCharcoal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "IDENTITY_INITIALIZATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    color = StoneGray
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Select Portal Type",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DeepCharcoal
                )
                Text(
                    text = "Define your role within the network architecture.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = StoneGray,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Organisation Role
                EliteRoleCard(
                    title = "ADMINISTRATOR",
                    description = "Establish a new workspace node and manage team execution pipelines.",
                    icon = Icons.Default.AddBusiness,
                    accentColor = DeepCharcoal,
                    onClick = onNavigateToCompanySignup
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Employee Role
                EliteRoleCard(
                    title = "OPERATIVE",
                    description = "Join an existing workspace using a secure access identifier code.",
                    icon = Icons.Default.PersonAdd,
                    accentColor = ProfessionalSuccess,
                    onClick = onNavigateToEmployeeSignup
                )
                
                Spacer(modifier = Modifier.height(60.dp))
                
                Text(
                    "v1.2.4 // SESSION_INIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmBorder
                )
                
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
fun EliteRoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accentColor.copy(alpha = 0.05f), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = accentColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    color = accentColor,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                    color = StoneGray,
                    lineHeight = 18.sp
                )
            }
            
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                tint = WarmBorder,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
