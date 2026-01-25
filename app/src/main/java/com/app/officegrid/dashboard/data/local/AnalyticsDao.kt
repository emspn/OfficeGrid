package com.app.officegrid.dashboard.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics WHERE companyId = :companyId")
    fun getAnalytics(companyId: String): Flow<AnalyticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: AnalyticsEntity)

    @Query("DELETE FROM analytics WHERE companyId = :companyId")
    suspend fun deleteAnalytics(companyId: String)
}