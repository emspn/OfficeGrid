package com.app.officegrid.employee.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.ui.theme.*

/**
 * 🎨 EMPLOYEE APP COMPONENT LIBRARY
 * Standardized, production-ready UI components
 */

// ============================================
// SECTION HEADERS
// ============================================

@Composable
fun EmployeeSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            ),
            color = DeepCharcoal
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp
                ),
                color = StoneGray
            )
        }
    }
}

// ============================================
// TOP BAR
// ============================================

@Composable
fun EmployeeTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp), // Tightened vertical padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = DeepCharcoal,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            ),
            color = DeepCharcoal,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
        Row {
            actions()
        }
    }
}

// ============================================
// STATUS BADGES
// ============================================

@Composable
fun EmployeeStatusBadge(
    status: TaskStatus,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status) {
        TaskStatus.TODO -> StoneGray
        TaskStatus.IN_PROGRESS -> ProfessionalWarning
        TaskStatus.PENDING_COMPLETION -> Color(0xFF2196F3)
        TaskStatus.DONE -> ProfessionalSuccess
    }

    val statusText = when (status) {
        TaskStatus.TODO -> "TODO"
        TaskStatus.IN_PROGRESS -> "IN PROGRESS"
        TaskStatus.PENDING_COMPLETION -> "AWAITING APPROVAL"
        TaskStatus.DONE -> "COMPLETED"
    }

    Surface(
        color = statusColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(statusColor, CircleShape)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = statusColor
            )
        }
    }
}

@Composable
fun EmployeePriorityBadge(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (priority) {
        TaskPriority.LOW -> StoneGray
        TaskPriority.MEDIUM -> ProfessionalWarning
        TaskPriority.HIGH -> ProfessionalError
    }

    Surface(
        color = priorityColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Text(
            text = priority.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = priorityColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ============================================
// TASK CARDS
// ============================================

@Composable
fun EmployeeTaskCard(
    title: String,
    status: TaskStatus,
    priority: TaskPriority,
    dueDate: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp, // Reduced elevation for cleaner look
        border = BorderStroke(1.dp, WarmBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp), // Reclaimed internal card space
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = DeepCharcoal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EmployeeStatusBadge(status = status)
                EmployeePriorityBadge(priority = priority)
            }

            if (dueDate != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = StoneGray
                    )
                    Text(
                        text = dueDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = StoneGray
                    )
                }
            }
        }
    }
}

// ============================================
// STAT CARDS
// ============================================

@Composable
fun EmployeeStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, WarmBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp
                ),
                color = DeepCharcoal
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = StoneGray
            )
        }
    }
}

// ============================================
// SEARCH BAR
// ============================================

@Composable
fun EmployeeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(20.dp),
                tint = StoneGray
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        modifier = Modifier.size(20.dp),
                        tint = StoneGray
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = DeepCharcoal,
            unfocusedBorderColor = WarmBorder
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

// ============================================
// EMPTY STATES
// ============================================

@Composable
fun EmployeeEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = WarmBorder
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = DeepCharcoal
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = StoneGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (actionButton != null) {
                Spacer(modifier = Modifier.height(8.dp))
                actionButton()
            }
        }
    }
}

// ============================================
// LOADING INDICATOR
// ============================================

@Composable
fun EmployeeLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = DeepCharcoal,
            strokeWidth = 2.dp,
            modifier = Modifier.size(size)
        )
    }
}

// ============================================
// ERROR SURFACE
// ============================================

@Composable
fun EmployeeErrorSurface(
    message: String,
    details: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = ProfessionalError
            )

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = ProfessionalError
            )

            if (details != null) {
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray
                )
            }

            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "RETRY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// ============================================
// INFO ITEM (for detail screens)
// ============================================

@Composable
fun EmployeeInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = StoneGray
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = StoneGray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = DeepCharcoal
            )
        }
    }
}
