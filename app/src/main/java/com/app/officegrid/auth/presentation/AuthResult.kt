package com.app.officegrid.auth.presentation

sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    object Success : AuthResult()
    object NotApproved : AuthResult()
    object EmailNotVerified : AuthResult()
    object InvalidCredentials : AuthResult()
    data class Error(val message: String) : AuthResult()
}
