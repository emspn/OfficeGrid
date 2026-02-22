package com.app.officegrid.auth.presentation

data class SignupUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val organisationName: String = "",
    val organisationType: String = "",
    val companyId: String = "",
    val authResult: AuthResult = AuthResult.Idle
)
