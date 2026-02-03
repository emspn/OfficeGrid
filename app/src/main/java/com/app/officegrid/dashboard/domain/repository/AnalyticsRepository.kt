package com.app.officegrid.dashboard.domain.repository

import com.app.officegrid.dashboard.domain.model.Analytics
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getAnalytics(companyId: String): Flow<Analytics?>
    suspend fun syncAnalytics(companyId: String): Result<Unit>
}
