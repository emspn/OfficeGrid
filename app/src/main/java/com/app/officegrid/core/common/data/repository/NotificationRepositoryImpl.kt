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

                // Get current user ID to filter notifications
                val currentUserId = getCurrentUserUseCase().first()?.id
                android.util.Log.d("NotificationRepo", "👤 Current user ID: $currentUserId")

                if (currentUserId == null) {
                    android.util.Log.e("NotificationRepo", "❌ Cannot start realtime sync - no user ID")
                    return@launch
                }

                android.util.Log.d("NotificationRepo", "✅ Subscribing to notification changes...")

                realtimeDataSource.subscribeToNotifications().collect { dto ->
                    android.util.Log.d("NotificationRepo", "📬 Received notification from Realtime:")
                    android.util.Log.d("NotificationRepo", "   → ID: ${dto.id}")
                    android.util.Log.d("NotificationRepo", "   → For User: ${dto.user_id}")
                    android.util.Log.d("NotificationRepo", "   → Title: ${dto.title}")
                    android.util.Log.d("NotificationRepo", "   → Current User: $currentUserId")

                    // ✅ CRITICAL FIX: Only process notifications meant for THIS user
                    if (dto.user_id != currentUserId) {
                        android.util.Log.d("NotificationRepo", "⏭️ Skipping - notification for different user")
                        return@collect // Skip this notification
                    }

                    android.util.Log.d("NotificationRepo", "✅ Notification is for THIS user - processing...")

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
                        // Save to database
                        notificationDao.insertNotifications(listOf(entity))
                        android.util.Log.d("NotificationRepo", "💾 Saved notification to local database")

                        // Show push notification
                        pushNotificationManager.showNotification(
                            id = entity.id,
                            title = entity.title,
                            message = entity.message,
                            type = entity.type
                        )
                        android.util.Log.d("NotificationRepo", "📱 Displayed OS notification")
                        android.util.Log.d("NotificationRepo", "✅✅✅ Notification fully processed!")
                    } else {
                        android.util.Log.d("NotificationRepo", "⏭️ Skipping - notification type disabled in settings")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationRepo", "💥 Realtime sync error: ${e.message}", e)
                e.printStackTrace()
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
            android.util.Log.d("NotificationRepo", "🔔 ATTEMPTING to send notification:")
            android.util.Log.d("NotificationRepo", "   → Recipient: $recipientId")
            android.util.Log.d("NotificationRepo", "   → Title: $title")
            android.util.Log.d("NotificationRepo", "   → Message: $message")
            android.util.Log.d("NotificationRepo", "   → Related ID: $relatedId")

            val user = getCurrentUserUseCase().first()
            if (user == null) {
                android.util.Log.e("NotificationRepo", "❌ User not authenticated - cannot send notification")
                return Result.failure(Exception("Not authenticated"))
            }

            android.util.Log.d("NotificationRepo", "   → Current User: ${user.id}")
            android.util.Log.d("NotificationRepo", "   → Company: ${user.companyId}")

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

            android.util.Log.d("NotificationRepo", "🚀 Inserting to Supabase...")
            postgrest["notifications"].insert(dto)
            android.util.Log.d("NotificationRepo", "✅ SUCCESS! Notification sent to Supabase")
            android.util.Log.d("NotificationRepo", "   → Notification ID: $notificationId")
            android.util.Log.d("NotificationRepo", "   → Will be received by user: $recipientId via Realtime")

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepo", "❌ FAILED to send notification: ${e.message}", e)
            e.printStackTrace()
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
