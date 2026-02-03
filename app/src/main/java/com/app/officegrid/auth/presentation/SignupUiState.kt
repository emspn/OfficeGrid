package com.app.officegrid.auth.presentation

data class SignupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
