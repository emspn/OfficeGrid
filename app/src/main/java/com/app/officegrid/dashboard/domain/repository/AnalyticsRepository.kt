package com.app.officegrid.dashboard.domain.repository

import com.app.officegrid.dashboard.domain.model.Analytics
import com.app.officegrid.dashboard.presentation.PerformanceItem
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getAnalytics(companyId: String): Flow<Analytics?>
    suspend fun syncAnalytics(companyId: String): Result<Unit>
    
    /**
     * ✅ Optimized: Fetches team performance pre-calculated from the DB
     */
    suspend fun getTeamPerformance(companyId: String): Result<List<PerformanceItem>>
}
