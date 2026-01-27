package com.app.officegrid.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.ui.theme.*

@Composable
fun WelcomeScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Refined Brand Anchor (consistent with Login)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DeepCharcoal, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "OFFICE_GRID",
                style = MaterialTheme.typography.displaySmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                ),
                color = DeepCharcoal
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Operational Visibility\n& Task Execution Platform",
                style = MaterialTheme.typography.titleLarge.copy(
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                ),
                color = MutedSlate
            )
            
            Text(
                text = "Enterprise-grade system for high-output teams.",
                style = MaterialTheme.typography.bodyLarge,
                color = StoneGray,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(60.dp))

            // Professional Aligned Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onNavigateToSignup,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
                ) {
                    Text(
                        "INITIALIZE_WORKSPACE", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepCharcoal)
                ) {
                    Text(
                        "RESTORE_SESSION", 
                        style = MaterialTheme.typography.labelMedium, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1.2f))
            
            Text(
                "v1.2.4 // SECURE_DISTRIBUTION",
                style = MaterialTheme.typography.labelSmall,
                color = WarmBorder
            )
        }
    }
}
