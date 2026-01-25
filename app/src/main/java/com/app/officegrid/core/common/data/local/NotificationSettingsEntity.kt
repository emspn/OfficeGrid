package com.app.officegrid.core.common.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey val userId: String,
    val taskAssigned: Boolean,
    val taskUpdated: Boolean,
    val taskOverdue: Boolean,
    val remarks: Boolean
)