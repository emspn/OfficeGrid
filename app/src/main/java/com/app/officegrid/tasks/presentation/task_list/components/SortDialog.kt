package com.app.officegrid.tasks.presentation.task_list.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.tasks.domain.model.TaskSortOption
import com.app.officegrid.tasks.domain.model.displayName
import com.app.officegrid.ui.theme.*

@Composable
fun SortDialog(
    currentSort: TaskSortOption,
    onSortSelected: (TaskSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmBackground,
        shape = RoundedCornerShape(4.dp),
        title = {
            Column {
                Text(
                    "SORT_OPTIONS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = DeepCharcoal
                )
                Text(
                    "Select sorting criteria",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                TaskSortOption.entries.forEach { option ->
                    SortOptionItem(
                        option = option,
                        isSelected = option == currentSort,
                        onClick = {
                            onSortSelected(option)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun SortOptionItem(
    option: TaskSortOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = if (isSelected) DeepCharcoal else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) DeepCharcoal else WarmBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when (option) {
                        TaskSortOption.DUE_DATE_ASC, TaskSortOption.DUE_DATE_DESC ->
                            Icons.Default.CalendarToday
                        TaskSortOption.PRIORITY_HIGH_FIRST, TaskSortOption.PRIORITY_LOW_FIRST ->
                            Icons.Default.Flag
                        TaskSortOption.TITLE_A_TO_Z, TaskSortOption.TITLE_Z_TO_A ->
                            Icons.Default.SortByAlpha
                        TaskSortOption.CREATED_NEWEST, TaskSortOption.CREATED_OLDEST ->
                            Icons.Default.AccessTime
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isSelected) Color.White else StoneGray
                )

                Text(
                    text = option.displayName(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else DeepCharcoal
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
    }
}
