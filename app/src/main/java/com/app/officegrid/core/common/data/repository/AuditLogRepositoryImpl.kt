package com.app.officegrid.core.common.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.data.local.AuditLogDao
import com.app.officegrid.core.common.data.local.AuditLogEntity
import com.app.officegrid.core.common.data.remote.dto.AuditLogDto
import com.app.officegrid.core.common.domain.model.AuditLog
import com.app.officegrid.core.common.domain.model.AuditEventType
import com.app.officegrid.core.common.domain.repository.AuditLogRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditLogRepositoryImpl @Inject constructor(
    private val auditLogDao: AuditLogDao,
    private val postgrest: Postgrest,
    private val authRepository: AuthRepository
) : AuditLogRepository {

    override fun getAuditLogs(): Flow<List<AuditLog>> {
        return auditLogDao.getAllAuditLogs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncAuditLogs(): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first() ?: throw Exception("Not authenticated")
            
            val remoteLogs = postgrest["audit_logs"]
                .select {
                    filter {
                        eq("company_id", user.companyId)
                    }
                }
                .decodeList<AuditLogDto>()

            val entities = remoteLogs.map { it.toEntity() }
            
            if (entities.isNotEmpty()) {
                auditLogDao.clearAuditLogs()
                auditLogDao.insertAuditLogs(entities)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createAuditLog(
        type: AuditEventType,
        title: String,
        description: String
    ): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first() ?: throw Exception("Not authenticated")
            
            val dto = AuditLogDto(
                action = title,
                entity_type = type.name,
                user_id = user.id,
                user_name = user.fullName,
                details = description,
                company_id = user.companyId
            )

            postgrest["audit_logs"].insert(dto)

            val entity = AuditLogEntity(
                id = java.util.UUID.randomUUID().toString(),
                eventType = type,
                title = title,
                description = description,
                userId = user.id,
                userEmail = user.email,
                createdAt = System.currentTimeMillis()
            )
            auditLogDao.insertAuditLogs(listOf(entity))

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
        id = id ?: java.util.UUID.randomUUID().toString(),
        eventType = try {
            AuditEventType.valueOf(entity_type.uppercase())
        } catch (e: Exception) {
            AuditEventType.CREATE 
        },
        title = action,
        description = details ?: "",
        userId = user_id,
        userEmail = user_name, // Mapping user_name to email field for UI compatibility
        createdAt = try {
            if (!created_at.isNullOrBlank()) {
                Instant.parse(created_at).toEpochMilli()
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )
}
