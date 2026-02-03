package com.app.officegrid.core.common.presentation

import com.app.officegrid.core.common.domain.model.AuditLog

data class AuditLogUiState(
    val isLoading: Boolean = false,
    val logs: List<AuditLog> = emptyList(),
    val error: String? = null
)
