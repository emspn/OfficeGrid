package com.app.officegrid.auth.presentation

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val authResult: AuthResult = AuthResult.Idle
)
