package com.app.officegrid.team.presentation

data class TeamUiState(
    val isLoading: Boolean = false,
    val members: List<String> = emptyList(),
    val error: String? = null
)