package com.app.officegrid.core.di

import com.app.officegrid.auth.data.repository.AuthRepositoryImpl
import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.data.repository.AuditLogRepositoryImpl
import com.app.officegrid.core.common.data.repository.NotificationRepositoryImpl
import com.app.officegrid.core.common.data.repository.NotificationSettingsRepositoryImpl
import com.app.officegrid.core.common.domain.repository.AuditLogRepository
import com.app.officegrid.core.common.domain.repository.NotificationRepository
import com.app.officegrid.core.common.domain.repository.NotificationSettingsRepository
import com.app.officegrid.dashboard.data.repository.AnalyticsRepositoryImpl
import com.app.officegrid.dashboard.domain.repository.AnalyticsRepository
import com.app.officegrid.tasks.data.repository.TaskRepositoryImpl
import com.app.officegrid.tasks.data.repository.TaskRemarkRepositoryImpl
import com.app.officegrid.tasks.domain.repository.TaskRepository
import com.app.officegrid.tasks.domain.repository.TaskRemarkRepository
import com.app.officegrid.team.data.repository.EmployeeRepositoryImpl
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        analyticsRepositoryImpl: AnalyticsRepositoryImpl
    ): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindNotificationSettingsRepository(
        notificationSettingsRepositoryImpl: NotificationSettingsRepositoryImpl
    ): NotificationSettingsRepository

    @Binds
    @Singleton
    abstract fun bindEmployeeRepository(
        employeeRepositoryImpl: EmployeeRepositoryImpl
    ): EmployeeRepository

    @Binds
    @Singleton
    abstract fun bindAuditLogRepository(
        auditLogRepositoryImpl: AuditLogRepositoryImpl
    ): AuditLogRepository

    @Binds
    @Singleton
    abstract fun bindTaskRemarkRepository(
        taskRemarkRepositoryImpl: TaskRemarkRepositoryImpl
    ): TaskRemarkRepository
}
