package com.app.officegrid.core.common.domain.repository

import com.app.officegrid.core.common.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {
    fun getSettings(): Flow<NotificationSettings?>
    suspend fun updateSettings(settings: NotificationSettings): Result<Unit>
    suspend fun syncSettings(): Result<Unit>
}
