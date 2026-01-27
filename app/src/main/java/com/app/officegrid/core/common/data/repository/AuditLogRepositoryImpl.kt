package com.app.officegrid.core.common.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.data.local.AuditLogDao
import com.app.officegrid.core.common.data.local.AuditLogEntity
import com.app.officegrid.core.common.domain.model.AuditLog
import com.app.officegrid.core.common.domain.repository.AuditLogRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AuditLogDto(
    val id: String,
    val event_type: String,
    val title: String,
    val description: String,
    val user_id: String,
    val user_email: String,
    val created_at: String
)

@Singleton
class AuditLogRepositoryImpl @Inject constructor(
    private val auditLogDao: AuditLogDao,
    private val postgrest: Postgrest?,
    private val authRepository: AuthRepository
) : AuditLogRepository {

    override fun getAuditLogs(): Flow<List<AuditLog>> {
        return auditLogDao.getAllAuditLogs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncAuditLogs(): Result<Unit> {
        return try {
            val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
            val user = authRepository.getCurrentUser().first() ?: throw Exception("Not authenticated")
            
            val remoteLogs = postgrestPlugin["audit_logs"]
                .select {
                    filter {
                        eq("company_id", user.companyId)
                    }
                }
                .decodeList<AuditLogDto>()

            val entities = remoteLogs.map { it.toEntity() }
            auditLogDao.clearAuditLogs()
            auditLogDao.insertAuditLogs(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun AuditLogEntity.toDomain() = AuditLog(
        id = id,
        eventType = eventType,
        title = title,
        description = description,
        userId = userId,
        userEmail = userEmail,
        createdAt = createdAt
    )

    private fun AuditLogDto.toEntity() = AuditLogEntity(
        id = id,
        eventType = try {
            com.app.officegrid.core.common.domain.model.AuditEventType.valueOf(event_type.uppercase())
        } catch (e: Exception) {
            com.app.officegrid.core.common.domain.model.AuditEventType.CREATE 
        },
        title = title,
        description = description,
        userId = user_id,
        userEmail = user_email,
        createdAt = try {
            Instant.parse(created_at).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )
}
