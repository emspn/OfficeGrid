package com.app.officegrid.core.common.data.remote

import com.app.officegrid.core.common.data.remote.dto.NotificationDto
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRemoteDataSourceRealtime @Inject constructor(
    private val realtime: Realtime?
) {
    fun subscribeToNotifications(): Flow<NotificationDto> {
        val realtimePlugin = realtime ?: throw Exception("Supabase Realtime not initialized")
        
        val channel = realtimePlugin.channel("notifications_channel")
        
        return channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "notifications"
        }.map { action ->
            Json.decodeFromJsonElement<NotificationDto>(action.record)
        }
    }
}