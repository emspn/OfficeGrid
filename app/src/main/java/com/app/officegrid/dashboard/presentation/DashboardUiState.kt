package com.app.officegrid.dashboard.presentation

import com.app.officegrid.dashboard.domain.model.Analytics

data class DashboardUiState(
    val isLoading: Boolean = false,
    val analytics: Analytics? = null,
    val error: String? = null
)
