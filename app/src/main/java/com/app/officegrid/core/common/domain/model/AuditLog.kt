package com.app.officegrid.core.common.domain.model

data class AuditLog(
    val id: String,
    val eventType: AuditEventType,
    val title: String,
    val description: String,
    val userId: String,
    val userEmail: String,
    val createdAt: Long
)

enum class AuditEventType {
    CREATE, UPDATE, DELETE, REMARK, STATUS_CHANGE
}
