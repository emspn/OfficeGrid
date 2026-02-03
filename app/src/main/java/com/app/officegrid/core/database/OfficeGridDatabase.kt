package com.app.officegrid.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.officegrid.core.common.data.local.AuditLogDao
import com.app.officegrid.core.common.data.local.AuditLogEntity
import com.app.officegrid.core.common.data.local.NotificationDao
import com.app.officegrid.core.common.data.local.NotificationEntity
import com.app.officegrid.core.common.data.local.NotificationSettingsDao
import com.app.officegrid.core.common.data.local.NotificationSettingsEntity
import com.app.officegrid.dashboard.data.local.AnalyticsDao
import com.app.officegrid.dashboard.data.local.AnalyticsEntity
import com.app.officegrid.tasks.data.local.TaskDao
import com.app.officegrid.tasks.data.local.TaskEntity
import com.app.officegrid.tasks.data.local.TaskRemarkDao
import com.app.officegrid.tasks.data.local.TaskRemarkEntity
import com.app.officegrid.team.data.local.EmployeeDao
import com.app.officegrid.team.data.local.EmployeeEntity

@Database(
    entities = [
        TaskEntity::class, 
        TaskRemarkEntity::class,
        EmployeeEntity::class,
        AnalyticsEntity::class,
        NotificationEntity::class,
        NotificationSettingsEntity::class,
        AuditLogEntity::class
    ],
    version = 6, // Incremented: EmployeeEntity now uses composite key (id + companyId) for multi-workspace
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OfficeGridDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
    abstract val taskRemarkDao: TaskRemarkDao
    abstract val employeeDao: EmployeeDao
    abstract val analyticsDao: AnalyticsDao
    abstract val notificationDao: NotificationDao
    abstract val notificationSettingsDao: NotificationSettingsDao
    abstract val auditLogDao: AuditLogDao

    companion object {
        const val DATABASE_NAME = "officegrid_db"
    }
}
