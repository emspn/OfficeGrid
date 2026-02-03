package com.app.officegrid.core.notification

import com.app.officegrid.core.common.NotificationType
import com.app.officegrid.core.common.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug helper to test notifications
 * Call this from UI to verify notification system works
 */
@Singleton
class NotificationTester @Inject constructor(
    private val notificationRepository: NotificationRepository
) {

    /**
     * Send a test notification to verify the system works
     * @param recipientId - User ID to send notification to
     */
    suspend fun sendTestNotification(recipientId: String): Result<Unit> {
        return notificationRepository.sendNotification(
            recipientId = recipientId,
            title = "🧪 Test Notification",
            message = "If you see this, notifications are working! Tap to dismiss.",
            type = NotificationType.TASK_UPDATED
        )
    }

    /**
     * Send a test task assignment notification
     */
    suspend fun sendTestTaskAssignment(recipientId: String): Result<Unit> {
        return notificationRepository.sendNotification(
            recipientId = recipientId,
            title = "🎯 Test Task Assigned",
            message = "This is a test task assignment notification. Your notification system is working!",
            type = NotificationType.TASK_ASSIGNED
        )
    }
}
