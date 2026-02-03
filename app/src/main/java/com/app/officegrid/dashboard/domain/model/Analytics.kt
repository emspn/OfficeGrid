package com.app.officegrid.dashboard.domain.model

data class Analytics(
    val totalTasks: Int,
    val completedTasks: Int,
    val inProgressTasks: Int,
    val pendingTasks: Int,
    val overdueTasks: Int,
    val tasksPerEmployee: Map<String, Int>,
    val completedTasksPerEmployee: Map<String, Int>
)
