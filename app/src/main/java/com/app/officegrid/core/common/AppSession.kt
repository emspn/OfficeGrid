package com.app.officegrid.core.common

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.tasks.domain.repository.TaskRepository
import com.app.officegrid.team.domain.repository.EmployeeRepository
import com.app.officegrid.core.common.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

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
    private val authRepository: AuthRepository,
    private val taskRepository: Lazy<TaskRepository>,
    private val employeeRepository: Lazy<EmployeeRepository>,
    private val notificationRepository: Lazy<NotificationRepository>
) {
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        checkInitialSession()
    }

    private fun checkInitialSession() {
        scope.launch {
            try {
                // Production-level session check: Parallel check
                val initialSession = authRepository.getSession()
                if (initialSession != null) {
                    _sessionState.update {
                        SessionState(
                            isInitializing = false,
                            isLoggedIn = true,
                            userRole = initialSession.user.role,
                            isApproved = initialSession.user.isApproved,
                            activeCompanyId = initialSession.user.companyId,
                            userId = initialSession.user.id
                        )
                    }
                }

                authRepository.getCurrentUser().collect { user ->
                    if (user != null) {
                        _sessionState.update { currentState ->
                            SessionState(
                                isInitializing = false,
                                isLoggedIn = true,
                                userRole = user.role,
                                isApproved = if (currentState.activeCompanyId == user.companyId) user.isApproved else currentState.isApproved,
                                activeCompanyId = currentState.activeCompanyId ?: user.companyId,
                                userId = user.id
                            )
                        }
                    } else {
                        // Only log out if we're not in the middle of initializing
                        val currentState = _sessionState.value
                        if (!currentState.isInitializing && currentState.isLoggedIn) {
                            _sessionState.update { SessionState(isInitializing = false) }
                        } else if (!currentState.isLoggedIn) {
                            _sessionState.update { it.copy(isInitializing = false) }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Session Initialization Failed")
                _sessionState.update { it.copy(isInitializing = false) }
            }
        }
    }

    fun currentState() = _sessionState.value

    fun switchWorkspace(companyId: String, isApproved: Boolean) {
        scope.launch {
            _sessionState.update { 
                it.copy(activeCompanyId = companyId, isApproved = isApproved)
            }
            authRepository.updateActiveCompany(companyId)
        }
    }

    /**
     * 🚀 PRODUCTION-LEVEL LOGOUT
     * Clears all layers: Auth, Database, and Memory
     */
    fun logout() {
        scope.launch(Dispatchers.IO) {
            try {
                Timber.d("LOGOUT: Initiating secure teardown...")
                
                // 1. Wipe Auth Session (Remote)
                authRepository.logout()
                
                // 2. Clear Local Persistence (Privacy/Security)
                taskRepository.get().clearLocalData()
                notificationRepository.get().clearAllNotifications()
                
                // 3. Reset Local State (UI)
                _sessionState.update { SessionState(isInitializing = false) }
                
                Timber.d("LOGOUT: Cleanup complete.")
            } catch (e: Exception) {
                Timber.e(e, "Logout error")
                // Force UI reset anyway
                _sessionState.update { SessionState(isInitializing = false) }
            }
        }
    }
}
