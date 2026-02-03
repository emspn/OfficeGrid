package com.app.officegrid.employee.presentation.join_workspace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.officegrid.ui.theme.*

/**
 * ✨ JOIN WORKSPACE DIALOG
 * Employee enters workspace code to join organization
 */
@Composable
fun JoinWorkspaceDialog(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var workspaceCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "JOIN WORKSPACE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = DeepCharcoal
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = StoneGray)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "Enter the workspace code provided by your organization admin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StoneGray
                )

                Spacer(Modifier.height(24.dp))

                // Workspace Code Input
                OutlinedTextField(
                    value = workspaceCode,
                    onValueChange = {
                        workspaceCode = it.uppercase().filter { c -> c.isLetterOrDigit() }
                    },
                    label = { Text("WORKSPACE CODE") },
                    placeholder = { Text("ABC123") },
                    leadingIcon = {
                        Icon(Icons.Default.Key, null, tint = DeepCharcoal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepCharcoal,
                        focusedLabelColor = DeepCharcoal
                    )
                )

                Spacer(Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isLoading
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            if (workspaceCode.isNotBlank()) {
                                isLoading = true
                                onJoin(workspaceCode.trim().uppercase())
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = workspaceCode.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfessionalSuccess
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "JOIN",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Info Text
                Surface(
                    color = ProfessionalSuccess.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "💡 After joining, wait for admin approval to access workspace tasks",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepCharcoal,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

