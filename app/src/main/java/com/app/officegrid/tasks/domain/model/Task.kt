package com.app.officegrid.tasks.domain.model

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignedTo: String,
    val createdBy: String,
    val companyId: String,
    val dueDate: Long
)

enum class TaskStatus {
    TODO, IN_PROGRESS, DONE
}

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}