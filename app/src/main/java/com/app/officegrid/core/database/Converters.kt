package com.app.officegrid.core.database

import androidx.room.TypeConverter
import com.app.officegrid.core.common.NotificationType
import com.app.officegrid.core.common.domain.model.AuditEventType
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus

class Converters {
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromNotificationType(type: NotificationType): String = type.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = NotificationType.valueOf(value)

    @TypeConverter
    fun fromAuditEventType(type: AuditEventType): String = type.name

    @TypeConverter
    fun toAuditEventType(value: String): AuditEventType = AuditEventType.valueOf(value)
}