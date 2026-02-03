package com.app.officegrid.core.common.data.remote

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class NotificationSettingsDto(
    val user_id: String,
    val task_assigned: Boolean = true,
    val task_updated: Boolean = true,
    val task_overdue: Boolean = true,
    val remarks: Boolean = true,
    val join_requests: Boolean = true,
    val system_notifications: Boolean = true
)

@Singleton
class SupabaseNotificationSettingsDataSource @Inject constructor(
    private val postgrest: Postgrest
) {
    suspend fun getSettings(): NotificationSettingsDto? {
        val postgrest = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        return postgrest["notification_settings"]
            .select()
            .decodeSingleOrNull<NotificationSettingsDto>()
    }

    suspend fun updateSettings(dto: NotificationSettingsDto) {
        val postgrest = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrest["notification_settings"].upsert(dto)
    }
}
