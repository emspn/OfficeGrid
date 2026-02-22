package com.app.officegrid.team.presentation

import com.app.officegrid.team.domain.model.Employee

// Team management UI state with approval messages
data class TeamUiState(
    val isLoading: Boolean = false,
    val pendingRequests: List<Employee> = emptyList(),
    val approvedMembers: List<Employee> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val currentUserId: String? = null
)
