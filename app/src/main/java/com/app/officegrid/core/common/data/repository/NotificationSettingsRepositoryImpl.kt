package com.app.officegrid.core.common.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.data.local.NotificationSettingsDao
import com.app.officegrid.core.common.data.local.NotificationSettingsEntity
import com.app.officegrid.core.common.data.remote.NotificationSettingsDto
import com.app.officegrid.core.common.data.remote.SupabaseNotificationSettingsDataSource
import com.app.officegrid.core.common.domain.model.NotificationSettings
import com.app.officegrid.core.common.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsRepositoryImpl @Inject constructor(
    private val settingsDao: NotificationSettingsDao,
    private val remoteDataSource: SupabaseNotificationSettingsDataSource,
    private val authRepository: AuthRepository
) : NotificationSettingsRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getSettings(): Flow<NotificationSettings?> {
        return authRepository.getCurrentUser().flatMapLatest { user ->
            if (user == null) {
                flowOf(null)
            } else {
                settingsDao.getSettings(user.id).map { it?.toDomain() }
            }
        }
    }

    override suspend fun updateSettings(settings: NotificationSettings): Result<Unit> {
        return try {
            val dto = settings.toDto()
            remoteDataSource.updateSettings(dto)
            settingsDao.insertSettings(settings.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncSettings(): Result<Unit> {
        return try {
            val remoteSettings = remoteDataSource.getSettings()
            if (remoteSettings != null) {
                settingsDao.insertSettings(remoteSettings.toEntity())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun NotificationSettingsEntity.toDomain() = NotificationSettings(
        userId = userId,
        taskAssigned = taskAssigned,
        taskUpdated = taskUpdated,
        taskOverdue = taskOverdue,
        remarks = remarks
    )

    private fun NotificationSettings.toEntity() = NotificationSettingsEntity(
        userId = userId,
        taskAssigned = taskAssigned,
        taskUpdated = taskUpdated,
        taskOverdue = taskOverdue,
        remarks = remarks
    )

    private fun NotificationSettings.toDto() = NotificationSettingsDto(
        user_id = userId,
        task_assigned = taskAssigned,
        task_updated = taskUpdated,
        task_overdue = taskOverdue,
        remarks = remarks
    )

    private fun NotificationSettingsDto.toEntity() = NotificationSettingsEntity(
        userId = user_id,
        taskAssigned = task_assigned,
        taskUpdated = task_updated,
        taskOverdue = task_overdue,
        remarks = remarks
    )
}