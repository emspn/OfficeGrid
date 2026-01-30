package com.app.officegrid.tasks.presentation.task_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.ui.theme.*

@Composable
fun PriorityFilterBar(
    selectedPriority: TaskPriority?,
    onPrioritySelected: (TaskPriority?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
        modifier = modifier
    ) {
        item {
            PriorityFilterChip(
                label = "ALL",
                isSelected = selectedPriority == null,
                color = DeepCharcoal,
                onClick = { onPrioritySelected(null) }
            )
        }
        items(TaskPriority.entries.toTypedArray()) { priority ->
            PriorityFilterChip(
                label = priority.name,
                isSelected = selectedPriority == priority,
                color = when(priority) {
                    TaskPriority.HIGH -> ProfessionalError
                    TaskPriority.MEDIUM -> ProfessionalWarning
                    TaskPriority.LOW -> DeepCharcoal
                },
                onClick = { onPrioritySelected(priority) }
            )
        }
    }
}

@Composable
fun PriorityFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) color else Color.White,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) color else WarmBorder
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (isSelected) Color.White else StoneGray
        )
    }
}
