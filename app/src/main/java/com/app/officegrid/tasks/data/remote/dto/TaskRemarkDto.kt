package com.app.officegrid.tasks.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskRemarkDto(
    val id: String? = null,
    val task_id: String,
    val user_id: String,
    val user_name: String,
    val content: String,
    val created_at: String? = null
)
