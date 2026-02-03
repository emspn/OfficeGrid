package com.app.officegrid.tasks.domain.model

enum class TaskSortOption {
    DUE_DATE_ASC,
    DUE_DATE_DESC,
    PRIORITY_HIGH_FIRST,
    PRIORITY_LOW_FIRST,
    TITLE_A_TO_Z,
    TITLE_Z_TO_A,
    CREATED_NEWEST,
    CREATED_OLDEST
}

fun List<Task>.sortByOption(option: TaskSortOption): List<Task> {
    return when (option) {
        TaskSortOption.DUE_DATE_ASC -> this.sortedBy { it.dueDate }
        TaskSortOption.DUE_DATE_DESC -> this.sortedByDescending { it.dueDate }
        TaskSortOption.PRIORITY_HIGH_FIRST -> this.sortedByDescending {
            when (it.priority) {
                TaskPriority.HIGH -> 3
                TaskPriority.MEDIUM -> 2
                TaskPriority.LOW -> 1
            }
        }
        TaskSortOption.PRIORITY_LOW_FIRST -> this.sortedBy {
            when (it.priority) {
                TaskPriority.HIGH -> 3
                TaskPriority.MEDIUM -> 2
                TaskPriority.LOW -> 1
            }
        }
        TaskSortOption.TITLE_A_TO_Z -> this.sortedBy { it.title.lowercase() }
        TaskSortOption.TITLE_Z_TO_A -> this.sortedByDescending { it.title.lowercase() }
        TaskSortOption.CREATED_NEWEST -> this.sortedByDescending { it.createdAt }
        TaskSortOption.CREATED_OLDEST -> this.sortedBy { it.createdAt }
    }
}

fun TaskSortOption.displayName(): String {
    return when (this) {
        TaskSortOption.DUE_DATE_ASC -> "Due Date (Earliest First)"
        TaskSortOption.DUE_DATE_DESC -> "Due Date (Latest First)"
        TaskSortOption.PRIORITY_HIGH_FIRST -> "Priority (High to Low)"
        TaskSortOption.PRIORITY_LOW_FIRST -> "Priority (Low to High)"
        TaskSortOption.TITLE_A_TO_Z -> "Title (A-Z)"
        TaskSortOption.TITLE_Z_TO_A -> "Title (Z-A)"
        TaskSortOption.CREATED_NEWEST -> "Recently Created"
        TaskSortOption.CREATED_OLDEST -> "Oldest First"
    }
}
