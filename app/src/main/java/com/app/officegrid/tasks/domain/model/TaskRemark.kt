package com.app.officegrid.tasks.domain.model

data class TaskRemark(
    val id: String,
    val taskId: String,
    val message: String,
    val createdBy: String,
    val createdAt: Long
)