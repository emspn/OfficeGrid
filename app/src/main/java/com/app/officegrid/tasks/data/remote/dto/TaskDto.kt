package com.app.officegrid.tasks.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: String? = null,
    val title: String,
    val description: String = "",
    val status: String = "TODO",
    val priority: String = "MEDIUM",
    val assigned_to: String,
    val created_by: String,
    val company_id: String,
    val due_date: String,
    val created_at: String? = null,
    val updated_at: String? = null
)
