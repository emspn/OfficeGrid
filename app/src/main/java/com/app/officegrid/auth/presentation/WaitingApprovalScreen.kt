package com.app.officegrid.auth.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.profile.presentation.ProfileViewModel
import com.app.officegrid.ui.theme.*

@Composable
fun WaitingApprovalScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hourglass")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Technical Status Indicator
            Surface(
                modifier = Modifier.size(100.dp),
                color = Color.White,
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .rotate(rotation),
                        tint = DeepCharcoal
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "NODE_ACCESS_PENDING",
                style = MaterialTheme.typography.titleLarge.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                ),
                color = DeepCharcoal,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "SYSTEM_VERIFICATION_IN_PROGRESS. ACCESS_GRANTED_UPON_ADMIN_AUTHORIZATION.",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(56.dp))

            // Operational Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Surface(
                    onClick = { /* Refresh logic */ },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp), tint = DeepCharcoal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "SYNCHRONIZE_STATUS", 
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepCharcoal
                        )
                    }
                }
                
                Surface(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp), tint = ProfessionalError)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "TERMINATE_SESSION", 
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ProfessionalError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "STATION_ID: ${System.currentTimeMillis().toString(36).uppercase()}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                color = WarmBorder
            )
        }
    }
}
