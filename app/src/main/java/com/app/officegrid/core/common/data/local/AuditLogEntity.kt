package com.app.officegrid.core.common.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.officegrid.core.common.domain.model.AuditEventType

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val eventType: AuditEventType,
    val title: String,
    val description: String,
    val userId: String,
    val userEmail: String,
    val createdAt: Long
)