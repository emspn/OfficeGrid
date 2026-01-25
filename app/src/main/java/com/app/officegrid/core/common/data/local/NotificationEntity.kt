package com.app.officegrid.core.common.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.officegrid.core.common.NotificationType

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val createdAt: Long,
    val isRead: Boolean
)