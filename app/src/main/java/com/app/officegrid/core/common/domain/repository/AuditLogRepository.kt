package com.app.officegrid.core.common.domain.repository

import com.app.officegrid.core.common.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun getAuditLogs(): Flow<List<AuditLog>>
    suspend fun syncAuditLogs(): Result<Unit>
}