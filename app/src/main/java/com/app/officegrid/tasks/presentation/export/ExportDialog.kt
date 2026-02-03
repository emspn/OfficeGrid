package com.app.officegrid.tasks.presentation.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.tasks.domain.usecase.ExportFormat
import com.app.officegrid.ui.theme.*

@Composable
fun ExportDialog(
    taskCount: Int,
    onExport: (ExportFormat) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmBackground,
        shape = RoundedCornerShape(4.dp),
        title = {
            Column {
                Text(
                    "EXPORT_TASKS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = DeepCharcoal
                )
                Text(
                    "$taskCount tasks will be exported",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "SELECT_FORMAT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MutedSlate
                )

                ExportFormatCard(
                    icon = Icons.Default.TableChart,
                    title = "CSV (Spreadsheet)",
                    description = "Excel compatible, best for data analysis",
                    onClick = {
                        onExport(ExportFormat.CSV)
                        onDismiss()
                    }
                )

                ExportFormatCard(
                    icon = Icons.Default.Description,
                    title = "Text File",
                    description = "Plain text format, human readable",
                    onClick = {
                        onExport(ExportFormat.TEXT)
                        onDismiss()
                    }
                )

                ExportFormatCard(
                    icon = Icons.Default.Code,
                    title = "Markdown",
                    description = "Formatted text with emojis",
                    onClick = {
                        onExport(ExportFormat.MARKDOWN)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "CANCEL",
                    style = MaterialTheme.typography.labelMedium,
                    color = StoneGray
                )
            }
        }
    )
}

@Composable
private fun ExportFormatCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = DeepCharcoal.copy(alpha = 0.05f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DeepCharcoal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = DeepCharcoal
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = StoneGray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = StoneGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

