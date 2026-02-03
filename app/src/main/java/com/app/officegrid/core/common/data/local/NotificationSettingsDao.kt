package com.app.officegrid.core.common.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationSettingsDao {
    @Query("SELECT * FROM notification_settings WHERE userId = :userId")
    fun getSettings(userId: String): Flow<NotificationSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: NotificationSettingsEntity)
}
