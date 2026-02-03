package com.app.officegrid.tasks.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String = "MEDIUM",
    val assigned_to: String,
    val created_by: String,
    val company_id: String,
    val due_date: String,  // ISO timestamp string for Supabase
    val created_at: String? = null
)
