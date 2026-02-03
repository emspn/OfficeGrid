package com.app.officegrid.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.officegrid.MainActivity
import com.app.officegrid.core.common.NotificationType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages local push notifications using Android's NotificationManager
 * Works with Supabase Realtime for background notification delivery
 */
@Singleton
class PushNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels for Android 8.0+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // High priority channel for task assignments and urgent updates
            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT,
                "Task Assignments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new task assignments and urgent updates"
                enableVibration(true)
                enableLights(true)
            }

            // Default priority channel for general updates
            val defaultChannel = NotificationChannel(
                CHANNEL_DEFAULT,
                "Task Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for task updates and comments"
                enableVibration(true)
            }

            // Low priority channel for informational messages
            val infoChannel = NotificationChannel(
                CHANNEL_INFO,
                "Information",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General information and reminders"
            }

            notificationManager.createNotificationChannels(
                listOf(urgentChannel, defaultChannel, infoChannel)
            )
        }
    }

    /**
     * Show a notification to the user
     */
    fun showNotification(
        id: String,
        title: String,
        message: String,
        type: NotificationType,
        data: Map<String, String> = emptyMap()
    ) {
        val channelId = getChannelIdForType(type)
        val priority = getPriorityForType(type)

        // Create intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add deep link data for navigation
            data.forEach { (key, value) -> putExtra(key, value) }
            putExtra("notification_type", type.name)
            putExtra("notification_id", id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, channelId)
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
        builder.setContentTitle(title)
        builder.setContentText(message)
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
        builder.priority = priority
        builder.setAutoCancel(true)
        builder.setContentIntent(pendingIntent)
        val notification = builder.build()

        // Show the notification
        notificationManager.notify(id.hashCode(), notification)
    }

    /**
     * Cancel a specific notification
     */
    fun cancelNotification(id: String) {
        notificationManager.cancel(id.hashCode())
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    /**
     * Get notification channel based on type
     */
    private fun getChannelIdForType(type: NotificationType): String {
        return when (type) {
            NotificationType.TASK_ASSIGNED,
            NotificationType.TASK_OVERDUE,
            NotificationType.JOIN_REQUEST -> CHANNEL_URGENT
            NotificationType.TASK_UPDATED,
            NotificationType.TASK_COMPLETED,
            NotificationType.NEW_REMARK,
            NotificationType.JOIN_APPROVED,
            NotificationType.JOIN_REJECTED -> CHANNEL_DEFAULT
            NotificationType.SYSTEM -> CHANNEL_INFO
        }
    }

    /**
     * Get notification priority based on type
     */
    private fun getPriorityForType(type: NotificationType): Int {
        return when (type) {
            NotificationType.TASK_ASSIGNED,
            NotificationType.TASK_OVERDUE,
            NotificationType.JOIN_REQUEST -> NotificationCompat.PRIORITY_HIGH
            NotificationType.TASK_UPDATED,
            NotificationType.TASK_COMPLETED,
            NotificationType.NEW_REMARK,
            NotificationType.JOIN_APPROVED,
            NotificationType.JOIN_REJECTED -> NotificationCompat.PRIORITY_DEFAULT
            NotificationType.SYSTEM -> NotificationCompat.PRIORITY_LOW
        }
    }

    companion object {
        private const val CHANNEL_URGENT = "officegrid_urgent"
        private const val CHANNEL_DEFAULT = "officegrid_default"
        private const val CHANNEL_INFO = "officegrid_info"
    }
}
