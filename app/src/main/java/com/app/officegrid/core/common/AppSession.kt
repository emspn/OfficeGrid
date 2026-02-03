package com.app.officegrid.core.common

import com.app.officegrid.auth.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class UserRole {
    ADMIN,
    EMPLOYEE
}

data class SessionState(
    val isInitializing: Boolean = true,
    val isLoggedIn: Boolean = false,
    val userRole: UserRole? = null,
    val isApproved: Boolean = false,
    val activeCompanyId: String? = null,
    val userId: String? = null
)

@Singleton
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository
) {
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        checkInitialSession()
    }

    private fun checkInitialSession() {
        scope.launch {
            // 1. Initial immediate check from cache
            val session = authRepository.getSession()
            if (session != null) {
                _sessionState.update {
                    SessionState(
                        isInitializing = false,
                        isLoggedIn = true,
                        userRole = session.user.role,
                        isApproved = session.user.isApproved,
                        activeCompanyId = session.user.companyId,
                        userId = session.user.id
                    )
                }
            }

            // 2. Observe real-time changes
            authRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    _sessionState.update { currentState ->
                        SessionState(
                            isInitializing = false,
                            isLoggedIn = true,
                            userRole = user.role,
                            isApproved = user.isApproved,
                            activeCompanyId = currentState.activeCompanyId ?: user.companyId,
                            userId = user.id
                        )
                    }
                } else {
                    // Only update to logged out if we're not currently initializing 
                    // or if it's an explicit logout/session loss
                    val wasInitializing = _sessionState.value.isInitializing
                    if (!wasInitializing) {
                        _sessionState.update { SessionState(isInitializing = false) }
                    } else {
                        // Just finish initialization
                        _sessionState.update { it.copy(isInitializing = false) }
                    }
                }
            }
        }
    }

    fun switchWorkspace(companyId: String, isApproved: Boolean) {
        _sessionState.update { 
            it.copy(activeCompanyId = companyId, isApproved = isApproved)
        }
    }

    fun logout() {
        scope.launch {
            authRepository.logout()
            _sessionState.update { SessionState(isInitializing = false) }
        }
    }
}
