package com.app.officegrid.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.NotificationType
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class OfficeGridNotificationService : Service() {

    @Inject
    lateinit var realtime: Realtime

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var pushNotificationManager: PushNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var realtimeJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startListening()
        return START_STICKY
    }

    private fun startListening() {
        if (realtimeJob?.isActive == true) return

        realtimeJob = serviceScope.launch {
            try {
                // Wait for a valid session to avoid 403s
                val user = authRepository.getCurrentUser().first() ?: return@launch
                
                android.util.Log.d("SentinelService", "⚡ Sentinel Active: Listening for User ${user.id}")
                
                val channel = realtime.channel("notifications_sentinel_${user.id}")
                
                // ✅ FIX: Define flow BEFORE subscribing
                val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "notifications"
                }

                android.util.Log.d("SentinelService", "📡 Subscribing to sentinel channel...")
                channel.subscribe()
                
                changeFlow.collect { action ->
                    val rawData = action.record
                    val recipientId = rawData["user_id"]?.toString()?.removeSurrounding("\"")
                    
                    if (recipientId == user.id) {
                        val title = rawData["title"]?.toString()?.removeSurrounding("\"") ?: "OfficeGrid Update"
                        val message = rawData["message"]?.toString()?.removeSurrounding("\"") ?: ""
                        val typeStr = rawData["type"]?.toString()?.removeSurrounding("\"") ?: "SYSTEM"
                        
                        val type = try { NotificationType.valueOf(typeStr) } catch (e: Exception) { NotificationType.SYSTEM }

                        pushNotificationManager.showNotification(
                            id = System.currentTimeMillis().toString(),
                            title = title,
                            message = message,
                            type = type
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SentinelService", "❌ Error in sentinel stream: ${e.message}")
                delay(10000) // Longer delay on error
                startListening()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "OfficeGrid Background Sentinel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the app connected to the secure data stream."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OfficeGrid Sentinel")
            .setContentText("Secure data stream active.")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val NOTIFICATION_ID = 999
        private const val CHANNEL_ID = "officegrid_sentinel_channel"

        fun start(context: Context) {
            val intent = Intent(context, OfficeGridNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, OfficeGridNotificationService::class.java)
            context.stopService(intent)
        }
    }
}
