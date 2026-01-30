package com.app.officegrid.employee.presentation.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.officegrid.ui.theme.*

@Composable
fun JoinWorkspaceDialog(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var workspaceCode by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = Color.White,
            shape = RoundedCornerShape(12.dp)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "JOIN_WORKSPACE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = DeepCharcoal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Enter workspace code to join",
                            style = MaterialTheme.typography.labelSmall,
                            color = StoneGray
                        )
                    }
                    if (!isLoading) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = StoneGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Info Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DeepCharcoal.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = DeepCharcoal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "How to get workspace code?",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepCharcoal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Ask your organization admin for the workspace code. It's usually displayed on their dashboard.",
                                style = MaterialTheme.typography.labelSmall,
                                color = StoneGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Workspace Code Input
                Text(
                    "WORKSPACE_CODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MutedSlate
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = workspaceCode,
                    onValueChange = { workspaceCode = it.trim().uppercase() },
                    placeholder = {
                        Text(
                            "Enter code (e.g., ABC123)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = DeepCharcoal
                        )
                    },
                    enabled = !isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = WarmBackground,
                        unfocusedContainerColor = WarmBackground,
                        disabledContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = DeepCharcoal,
                        unfocusedIndicatorColor = WarmBorder,
                        cursorColor = DeepCharcoal
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Error Message
                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ProfessionalError.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = ProfessionalError,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                error,
                                style = MaterialTheme.typography.labelSmall,
                                color = ProfessionalError
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        )
                    ) {
                        Text(
                            "CANCEL",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Button(
                        onClick = { if (workspaceCode.isNotBlank()) onJoin(workspaceCode) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isLoading && workspaceCode.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepCharcoal,
                            disabledContainerColor = StoneGray
                        ),
                        shape = RoundedCornerShape(8.dp)
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
            }
        }
    }
}
