package com.app.officegrid.dashboard.domain.model

data class Analytics(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val pendingTasks: Int = 0,
    val totalEmployees: Int = 0,
    val pendingApprovals: Int = 0,
    val overdueTasks: Int = 0,
    val tasksPerEmployee: Map<String, Int> = emptyMap(),
    val completedTasksPerEmployee: Map<String, Int> = emptyMap()
)
