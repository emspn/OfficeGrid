package com.app.officegrid.core.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

enum class UserRole {
    ADMIN,
    EMPLOYEE
}

data class SessionState(
    val isLoggedIn: Boolean = false,
    val userRole: UserRole? = null
)

@Singleton
class SessionManager @Inject constructor() {
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    fun login(role: UserRole) {
        _sessionState.update { 
            it.copy(isLoggedIn = true, userRole = role)
        }
    }

    fun logout() {
        _sessionState.update { 
            it.copy(isLoggedIn = false, userRole = null)
        }
    }
}