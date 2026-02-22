package com.app.officegrid.core.common.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuditLogDto(
    val id: String? = null,
    val action: String,
    val entity_type: String,
    val entity_id: String? = null,
    val user_id: String,
    val user_name: String,
    val details: String? = null,
    val company_id: String,
    val created_at: String? = null
)
