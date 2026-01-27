package com.app.officegrid.team.presentation

import com.app.officegrid.team.domain.model.Employee

data class TeamUiState(
    val isLoading: Boolean = false,
    val pendingRequests: List<Employee> = emptyList(),
    val approvedMembers: List<Employee> = emptyList(),
    val error: String? = null
)