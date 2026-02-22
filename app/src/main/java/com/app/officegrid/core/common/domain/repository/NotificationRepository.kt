package com.app.officegrid.core.common.domain.repository

import com.app.officegrid.core.common.AppNotification
import com.app.officegrid.core.common.NotificationType
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<AppNotification>>
    fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
    suspend fun syncNotifications(): Result<Unit>
    
    /**
     * Send a notification to a specific user
     * @param recipientId The user ID to send the notification to
     * @param title Notification title
     * @param message Notification message body
     * @param type Type of notification for categorization
     * @param relatedId Optional ID of related entity (task, employee, etc.) for deep linking
     */
    suspend fun sendNotification(
        recipientId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedId: String? = null
    ): Result<Unit>

    suspend fun deleteNotification(notificationId: String): Result<Unit>
    suspend fun clearAllNotifications(): Result<Unit>

    fun getNotificationsByType(type: NotificationType): Flow<List<AppNotification>>
    
    suspend fun registerFCMToken(token: String): Result<Unit>
}
