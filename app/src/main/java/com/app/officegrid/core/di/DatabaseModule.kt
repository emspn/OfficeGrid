package com.app.officegrid.core.di

import android.content.Context
import androidx.room.Room
import com.app.officegrid.core.common.data.local.AuditLogDao
import com.app.officegrid.core.common.data.local.NotificationDao
import com.app.officegrid.core.common.data.local.NotificationSettingsDao
import com.app.officegrid.core.database.OfficeGridDatabase
import com.app.officegrid.dashboard.data.local.AnalyticsDao
import com.app.officegrid.tasks.data.local.TaskDao
import com.app.officegrid.tasks.data.local.TaskRemarkDao
import com.app.officegrid.team.data.local.EmployeeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OfficeGridDatabase {
        return Room.databaseBuilder(
            context,
            OfficeGridDatabase::class.java,
            OfficeGridDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(db: OfficeGridDatabase): TaskDao = db.taskDao

    @Provides
    @Singleton
    fun provideTaskRemarkDao(db: OfficeGridDatabase): TaskRemarkDao = db.taskRemarkDao

    @Provides
    @Singleton
    fun provideEmployeeDao(db: OfficeGridDatabase): EmployeeDao = db.employeeDao

    @Provides
    @Singleton
    fun provideAnalyticsDao(db: OfficeGridDatabase): AnalyticsDao = db.analyticsDao

    @Provides
    @Singleton
    fun provideNotificationDao(db: OfficeGridDatabase): NotificationDao = db.notificationDao

    @Provides
    @Singleton
    fun provideNotificationSettingsDao(db: OfficeGridDatabase): NotificationSettingsDao = db.notificationSettingsDao

    @Provides
    @Singleton
    fun provideAuditLogDao(db: OfficeGridDatabase): AuditLogDao = db.auditLogDao
}
