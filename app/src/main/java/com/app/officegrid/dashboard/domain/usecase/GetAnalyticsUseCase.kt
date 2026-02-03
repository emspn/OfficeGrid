package com.app.officegrid.dashboard.domain.usecase

import com.app.officegrid.dashboard.domain.model.Analytics
import com.app.officegrid.dashboard.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAnalyticsUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    operator fun invoke(companyId: String): Flow<Analytics?> {
        return repository.getAnalytics(companyId)
    }
}
