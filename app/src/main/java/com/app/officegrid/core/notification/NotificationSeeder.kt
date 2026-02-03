package com.app.officegrid.core.notification

import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.common.NotificationType
import com.app.officegrid.core.common.data.local.NotificationDao
import com.app.officegrid.core.common.data.local.NotificationEntity
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds sample notifications for testing
 */
@Singleton
class NotificationSeeder @Inject constructor(
    private val notificationDao: NotificationDao,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {

    /**
     * Create sample notifications for testing
     */
    suspend fun seedSampleNotifications() {
        try {
            val user = getCurrentUserUseCase().first() ?: return

            val sampleNotifications = listOf(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    title = "🎯 Welcome to OfficeGrid!",
                    message = "Your notification system is ready. You'll receive updates for task assignments, completions, and comments here.",
                    type = NotificationType.TASK_UPDATED,
                    createdAt = System.currentTimeMillis(),
                    isRead = false
                ),
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    title = "✅ Sample Task Completed",
                    message = "This is how task completion notifications will look. Great work!",
                    type = NotificationType.TASK_COMPLETED,
                    createdAt = System.currentTimeMillis() - 3600000, // 1 hour ago
                    isRead = false
                ),
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    title = "💬 Sample Comment",
                    message = "John Doe left a comment on 'Sample Task'. This is how comment notifications appear.",
                    type = NotificationType.NEW_REMARK,
                    createdAt = System.currentTimeMillis() - 7200000, // 2 hours ago
                    isRead = false
                )
            )

            notificationDao.insertNotifications(sampleNotifications)
            android.util.Log.d("NotificationSeeder", "✅ Seeded ${sampleNotifications.size} sample notifications")
        } catch (e: Exception) {
            android.util.Log.e("NotificationSeeder", "❌ Failed to seed notifications: ${e.message}", e)
        }
    }

    /**
     * Clear all notifications (for testing)
     */
    suspend fun clearAllNotifications() {
        try {
            notificationDao.clearAll()
            android.util.Log.d("NotificationSeeder", "✅ Cleared all notifications")
        } catch (e: Exception) {
            android.util.Log.e("NotificationSeeder", "❌ Failed to clear notifications: ${e.message}", e)
        }
    }
}
