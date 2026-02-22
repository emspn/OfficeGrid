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
import com.app.officegrid.core.notification.PushNotificationManager
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val settingsRepository: NotificationSettingsRepository,
    private val pushNotificationManager: PushNotificationManager,
    private val postgrest: Postgrest
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
                        // Small delay to allow Auth headers to propagate
                        delay(1000)
                        syncNotifications() // ✅ Fetch missed notifications on startup/reconnect
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
                android.util.Log.d("NotificationRepo", "🔄 Starting Realtime notification sync...")

                val currentUserId = getCurrentUserUseCase().first()?.id
                if (currentUserId == null) return@launch

                realtimeDataSource.subscribeToNotifications().collect { dto ->
                    if (dto.user_id != currentUserId) return@collect

                    val entity = dto.toEntity()
                    val settings = settingsRepository.getSettings().first()
                    
                    val shouldSave = when (entity.type) {
                        NotificationType.TASK_ASSIGNED -> settings?.taskAssigned ?: true
                        NotificationType.TASK_UPDATED, NotificationType.TASK_COMPLETED -> settings?.taskUpdated ?: true
                        NotificationType.TASK_OVERDUE -> settings?.taskOverdue ?: true
                        NotificationType.NEW_REMARK -> settings?.remarks ?: true
                        NotificationType.JOIN_REQUEST, NotificationType.JOIN_APPROVED, NotificationType.JOIN_REJECTED -> settings?.joinRequests ?: true
                        NotificationType.SYSTEM -> settings?.systemNotifications ?: true
                    }

                    if (shouldSave) {
                        // Check if already exists to avoid duplicates from FCM vs Realtime
                        val existing = notificationDao.getNotificationByIdSync(entity.id)
                        if (existing == null) {
                            notificationDao.insertNotifications(listOf(entity))
                            pushNotificationManager.showNotification(
                                id = entity.id,
                                title = entity.title,
                                message = entity.message,
                                type = entity.type
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationRepo", "💥 Realtime sync error: ${e.message}")
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

    /**
     * ✅ RECOVERY LOGIC: Fetches missed notifications from the remote database.
     * This ensures that if the device was offline, we pull everything that happened
     * during the downtime.
     */
    override suspend fun syncNotifications(): Result<Unit> {
        return try {
            val user = getCurrentUserUseCase().first() ?: return Result.failure(Exception("Not authenticated"))
            
            android.util.Log.d("NotificationRepo", "📥 Syncing missed notifications for user: ${user.id}")
            
            val remoteNotifications = postgrest["notifications"]
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<NotificationDto>()

            val entities = remoteNotifications.map { it.toEntity() }
            
            // Atomic update: only insert ones that don't exist
            if (entities.isNotEmpty()) {
                val settings = settingsRepository.getSettings().first()
                val filteredEntities = entities.filter { entity ->
                    when (entity.type) {
                        NotificationType.TASK_ASSIGNED -> settings?.taskAssigned ?: true
                        NotificationType.TASK_UPDATED, NotificationType.TASK_COMPLETED -> settings?.taskUpdated ?: true
                        NotificationType.TASK_OVERDUE -> settings?.taskOverdue ?: true
                        NotificationType.NEW_REMARK -> settings?.remarks ?: true
                        NotificationType.JOIN_REQUEST, NotificationType.JOIN_APPROVED, NotificationType.JOIN_REJECTED -> settings?.joinRequests ?: true
                        NotificationType.SYSTEM -> settings?.systemNotifications ?: true
                    }
                }
                
                notificationDao.insertNotifications(filteredEntities)
                android.util.Log.d("NotificationRepo", "✅ Sync complete. Found ${filteredEntities.size} notifications.")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepo", "❌ Sync failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationDao.deleteNotification(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllNotifications(): Result<Unit> {
        return try {
            notificationDao.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNotificationsByType(type: NotificationType): Flow<List<AppNotification>> {
        return notificationDao.getNotificationsByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun sendNotification(
        recipientId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedId: String?
    ): Result<Unit> {
        return try {
            val user = getCurrentUserUseCase().first() ?: return Result.failure(Exception("Not authenticated"))
            val notificationId = java.util.UUID.randomUUID().toString()

            val dto = NotificationDto(
                id = notificationId,
                user_id = recipientId,
                title = title,
                message = message,
                type = type.name,
                company_id = user.companyId,
                is_read = false,
                created_at = null,
                related_id = relatedId
            )

            postgrest["notifications"].insert(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerFCMToken(token: String): Result<Unit> {
        return try {
            val user = getCurrentUserUseCase().first() ?: return Result.failure(Exception("Not authenticated"))
            postgrest["employees"].update(
                mapOf("fcm_token" to token)
            ) {
                filter { eq("id", user.id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepo", "Failed to register FCM token: ${e.message}")
            Result.failure(e)
        }
    }

    private fun NotificationEntity.toDomain() = AppNotification(
        id = id,
        userId = userId,
        title = title,
        message = message,
        type = type,
        createdAt = createdAt,
        isRead = isRead,
        relatedId = relatedId
    )

    private fun NotificationDto.toEntity() = NotificationEntity(
        id = id,
        userId = user_id,
        title = title,
        message = message,
        type = try { NotificationType.valueOf(type) } catch (e: Exception) { NotificationType.TASK_UPDATED },
        createdAt = System.currentTimeMillis(),
        isRead = is_read,
        relatedId = related_id
    )
}
