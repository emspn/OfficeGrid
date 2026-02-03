package com.app.officegrid.core.common

data class AppNotification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val createdAt: Long,
    val isRead: Boolean,
    val relatedId: String? = null // For deep linking to tasks, etc.
)
