package com.app.officegrid.tasks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignedTo: String,
    val createdBy: String,
    val companyId: String,
    val dueDate: Long,
    val createdAt: Long = 0L
)
