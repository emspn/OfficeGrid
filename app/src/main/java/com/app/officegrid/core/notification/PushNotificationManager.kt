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
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🚀 ULTRA PRODUCTION-READY PUSH NOTIFICATION MANAGER
 * Manages notification delivery, channels, and intent routing with deep safety.
 */
@Singleton
class PushNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val recentNotificationIds = ConcurrentHashMap<String, Long>()

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 🎯 URGENT: Mission Assignments & Authorizations
            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT,
                "Mission Critical Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts for mission assignments and access requests."
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // 📝 DEFAULT: Communications & Status Logs
            val defaultChannel = NotificationChannel(
                CHANNEL_DEFAULT,
                "Operational Communications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Standard updates for mission status and collaborator remarks."
                enableVibration(true)
                setShowBadge(true)
            }

            // ℹ️ INFO: System Maintenance
            val infoChannel = NotificationChannel(
                CHANNEL_INFO,
                "System Registry Info",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General system information and registry maintenance alerts."
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(
                listOf(urgentChannel, defaultChannel, infoChannel)
            )
        }
    }

    fun showNotification(
        id: String,
        title: String,
        message: String,
        type: NotificationType,
        data: Map<String, String> = emptyMap()
    ) {
        val now = System.currentTimeMillis()
        val lastShownAt = recentNotificationIds[id]
        if (lastShownAt != null && (now - lastShownAt) < DEDUP_WINDOW_MS) {
            // Duplicate payload from another transport path (Realtime/FCM) in a short window.
            return
        }
        recentNotificationIds[id] = now
        cleanupOldDedupEntries(now)

        val channelId = getChannelIdForType(type)
        
        // 🔥 PRODUCTION-GRADE INTENT ROUTING
        val intent = Intent(context, MainActivity::class.java).apply {
            // Clears activity stack to handle fresh entry on tap
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            
            // Deep Link Parameters
            data.forEach { (key, value) -> putExtra(key, value) }
            putExtra("EXTRA_NOTIF_ID", id)
            putExtra("EXTRA_NOTIF_TYPE", type.name)
            
            // Rebranding sync
            if (data.containsKey("task_id")) {
                putExtra("MISSION_ID", data["task_id"])
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            // Tactical Iconography
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title.uppercase()) // Force professional uppercase
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(getPriorityForType(type))
            .setCategory(getCategoryForType(type))
            .setAutoCancel(true)
            .setDefaults(android.app.Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            // Color branding
            .setColor(0xFF1A1A1A.toInt()) // DeepCharcoal
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Show with unique hash to prevent overwriting other notifications
        notificationManager.notify(id.hashCode(), builder.build())
    }

    private fun cleanupOldDedupEntries(now: Long) {
        recentNotificationIds.entries.removeIf { (_, timestamp) ->
            (now - timestamp) > DEDUP_WINDOW_MS
        }
    }

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

    private fun getPriorityForType(type: NotificationType): Int {
        return when (type) {
            NotificationType.TASK_ASSIGNED,
            NotificationType.TASK_OVERDUE,
            NotificationType.JOIN_REQUEST -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    private fun getCategoryForType(type: NotificationType): String {
        return when (type) {
            NotificationType.TASK_ASSIGNED -> NotificationCompat.CATEGORY_MESSAGE
            NotificationType.JOIN_REQUEST -> NotificationCompat.CATEGORY_EVENT
            else -> NotificationCompat.CATEGORY_STATUS
        }
    }

    companion object {
        private const val CHANNEL_URGENT = "officegrid_urgent_v2"
        private const val CHANNEL_DEFAULT = "officegrid_default_v2"
        private const val CHANNEL_INFO = "officegrid_info_v2"
        private const val DEDUP_WINDOW_MS = 2 * 60 * 1000L
    }
}
