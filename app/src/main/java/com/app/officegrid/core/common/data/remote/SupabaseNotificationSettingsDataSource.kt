package com.app.officegrid.core.common.data.remote

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class NotificationSettingsDto(
    val user_id: String,
    val task_assigned: Boolean,
    val task_updated: Boolean,
    val task_overdue: Boolean,
    val remarks: Boolean
)

@Singleton
class SupabaseNotificationSettingsDataSource @Inject constructor(
    private val postgrest: Postgrest?
) {
    suspend fun getSettings(): NotificationSettingsDto? {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        return postgrestPlugin["notification_settings"]
            .select()
            .decodeSingleOrNull<NotificationSettingsDto>()
    }

    suspend fun updateSettings(dto: NotificationSettingsDto) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin["notification_settings"].upsert(dto)
    }
}