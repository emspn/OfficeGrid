package com.app.officegrid.core.common.domain.model

data class NotificationSettings(
    val userId: String,
    val taskAssigned: Boolean = true,
    val taskUpdated: Boolean = true,
    val taskOverdue: Boolean = true,
    val remarks: Boolean = true
)