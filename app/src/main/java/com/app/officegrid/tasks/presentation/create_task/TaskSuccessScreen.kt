package com.app.officegrid.tasks.presentation.create_task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🚀 MISSION SUCCESS SCREEN
 * Redesigned for a clean, professional, and centered "Receipt" look.
 */
@Composable
fun TaskSuccessScreen(
    taskTitle: String,
    assignedToName: String,
    dueDate: Long,
    onDone: () -> Unit
) {
    val timestamp = remember { 
        SimpleDateFormat("dd MMMM yyyy 'at' hh:mm a", Locale.getDefault()).format(Date()) 
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // 1. Center Checkmark
            Surface(
                modifier = Modifier.size(90.dp),
                shape = CircleShape,
                color = ProfessionalSuccess.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ProfessionalSuccess,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // 2. Mission Status
            Text(
                text = "MISSION_INITIALIZED",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = DeepCharcoal,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = timestamp.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                ),
                color = StoneGray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(48.dp))

            // 3. Assignment Details Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = DeepCharcoal.copy(alpha = 0.05f)
                        ) {
                            Icon(Icons.Default.Shield, null, modifier = Modifier.padding(8.dp), tint = DeepCharcoal)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = assignedToName.uppercase(), 
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = "AUTHORIZED_OPERATIVE", 
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), 
                                color = StoneGray
                            )
                        }
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 20.dp), color = WarmBorder, thickness = 0.5.dp)
                    
                    Text(
                        text = taskTitle.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black, 
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 24.sp
                        ),
                        color = DeepCharcoal
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DEADLINE: ", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = StoneGray)
                        Text(
                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dueDate)),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black),
                            color = ProfessionalError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. AD SPACE (Monetization Area)
            Surface(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "SPONSORED_COMMUNICATION", 
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 8.sp), 
                            color = WarmBorder
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "RESERVED_FOR_GRID_PARTNERS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                            color = WarmBorder.copy(alpha = 0.5f)
                        )
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                        color = Gray100.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text("Ad", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp, color = StoneGray)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. Action Button
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "DONE", 
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                )
            }
        }
    }
}
