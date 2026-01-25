package com.app.officegrid.dashboard.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analytics")
data class AnalyticsEntity(
    @PrimaryKey val companyId: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val inProgressTasks: Int,
    val pendingTasks: Int,
    val overdueTasks: Int,
    val tasksPerEmployeeJson: String, // Stored as JSON string
    val completedTasksPerEmployeeJson: String, // Stored as JSON string
    val updatedAt: Long
)