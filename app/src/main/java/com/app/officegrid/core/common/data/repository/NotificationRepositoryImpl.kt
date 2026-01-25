package com.app.officegrid.core.common.data.repository

import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.common.AppNotification
import com.app.officegrid.core.common.NotificationType
import com.app.officegrid.core.common.data.local.NotificationDao
import com.app.officegrid.core.common.data.local.NotificationEntity
import com.app.officegrid.core.common.data.remote.NotificationRemoteDataSourceRealtime
import com.app.officegrid.core.common.data.remote.dto.NotificationDto
import com.app.officegrid.core.common.domain.repository.NotificationRepository
import com.app.officegrid.core.common.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val realtimeDataSource: NotificationRemoteDataSourceRealtime,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val settingsRepository: NotificationSettingsRepository
) : NotificationRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var realtimeJob: Job? = null

    init {
        observeSession()
    }

    private fun observeSession() {
        scope.launch {
            getCurrentUserUseCase()
                .map { it?.id }
                .distinctUntilChanged()
                .collectLatest { userId ->
                    if (userId != null) {
                        startRealtimeSync()
                    } else {
                        stopRealtimeSync()
                    }
                }
        }
    }

    private fun startRealtimeSync() {
        realtimeJob?.cancel()
        realtimeJob = scope.launch {
            try {
                realtimeDataSource.subscribeToNotifications().collect { dto ->
                    val entity = dto.toEntity()
                    val settings = settingsRepository.getSettings().first()
                    
                    val shouldSave = when (entity.type) {
                        NotificationType.TASK_ASSIGNED -> settings?.taskAssigned ?: true
                        NotificationType.TASK_UPDATED, NotificationType.TASK_COMPLETED -> settings?.taskUpdated ?: true
                        NotificationType.TASK_OVERDUE -> settings?.taskOverdue ?: true
                        NotificationType.NEW_REMARK -> settings?.remarks ?: true
                    }

                    if (shouldSave) {
                        notificationDao.insertNotifications(listOf(entity))
                    }
                }
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    private fun stopRealtimeSync() {
        realtimeJob?.cancel()
        realtimeJob = null
    }

    override fun getNotifications(): Flow<List<AppNotification>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationDao.markAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            notificationDao.markAllAsRead()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncNotifications(): Result<Unit> {
        return Result.success(Unit)
    }

    private fun NotificationEntity.toDomain() = AppNotification(
        id = id,
        userId = userId,
        title = title,
        message = message,
        type = type,
        createdAt = createdAt,
        isRead = isRead
    )

    private fun NotificationDto.toEntity() = NotificationEntity(
        id = id,
        userId = user_id,
        title = title,
        message = message,
        type = try { NotificationType.valueOf(type) } catch (e: Exception) { NotificationType.TASK_UPDATED },
        createdAt = System.currentTimeMillis(),
        isRead = is_read
    )
}