package com.app.officegrid.core.common.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey val userId: String,
    val taskAssigned: Boolean = true,
    val taskUpdated: Boolean = true,
    val taskOverdue: Boolean = true,
    val remarks: Boolean = true,
    val joinRequests: Boolean = true,
    val systemNotifications: Boolean = true
)
