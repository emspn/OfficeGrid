package com.app.officegrid.analytics.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsDto(
    val id: String? = null,
    val company_id: String,
    val total_tasks: Int = 0,
    val completed_tasks: Int = 0,
    val pending_tasks: Int = 0,
    val in_progress_tasks: Int = 0,
    val total_employees: Int = 0,
    val pending_approvals: Int = 0,
    val updated_at: String? = null
)
