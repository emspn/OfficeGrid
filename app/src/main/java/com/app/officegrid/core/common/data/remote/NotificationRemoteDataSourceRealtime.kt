package com.app.officegrid.core.common.data.remote

import com.app.officegrid.core.common.data.remote.dto.NotificationDto
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRemoteDataSourceRealtime @Inject constructor(
    private val realtime: Realtime
) {
    private var channel: RealtimeChannel? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun subscribeToNotifications(): Flow<NotificationDto> = flow {
        android.util.Log.d("NotificationRealtime", "🔄 Setting up notification channel...")

        // Create channel if not exists
        if (channel == null) {
            channel = realtime.channel("notifications_channel")
        }

        val notificationChannel = channel!!

        // ✅ CRITICAL FIX: postgresChangeFlow MUST be called BEFORE subscribe()
        val changeFlow = notificationChannel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "notifications"
        }

        // Subscribe to the channel
        android.util.Log.d("NotificationRealtime", "📡 Subscribing to channel...")
        notificationChannel.subscribe()
        android.util.Log.d("NotificationRealtime", "✅ Channel subscribed successfully!")

        // Collect and emit notifications from the flow defined BEFORE subscription
        changeFlow.collect { action ->
            try {
                android.util.Log.d("NotificationRealtime", "📬 Received INSERT event: ${action.record}")
                val dto = json.decodeFromJsonElement<NotificationDto>(action.record)
                android.util.Log.d("NotificationRealtime", "✅ Parsed notification: ${dto.title}")
                emit(dto)
            } catch (e: Exception) {
                android.util.Log.e("NotificationRealtime", "❌ Failed to parse notification: ${e.message}", e)
            }
        }
    }.catch { e ->
        android.util.Log.e("NotificationRealtime", "❌ Realtime error: ${e.message}", e)
    }

    fun unsubscribe() {
        channel?.let {
            android.util.Log.d("NotificationRealtime", "🔌 Unsubscribing from notification channel...")
            channel = null
        }
    }
}
