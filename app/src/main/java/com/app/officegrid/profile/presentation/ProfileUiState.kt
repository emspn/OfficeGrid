package com.app.officegrid.profile.presentation

data class ProfileUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val error: String? = null
)
