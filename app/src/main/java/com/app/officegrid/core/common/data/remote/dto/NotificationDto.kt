package com.app.officegrid.core.common.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String,
    val user_id: String,
    val title: String,
    val message: String,
    val type: String,
    val company_id: String,
    val is_read: Boolean = false,
    val created_at: String? = null
)