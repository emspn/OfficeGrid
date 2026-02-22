package com.app.officegrid.core.common.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationSettingsDto(
    val id: String? = null,
    val user_id: String,
    val task_assigned: Boolean = true,
    val task_updated: Boolean = true,
    val task_overdue: Boolean = true,
    val remarks: Boolean = true,
    val join_requests: Boolean = true,
    val system_notifications: Boolean = true,
    val updated_at: String? = null
)
