package com.app.officegrid.core.common

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.tasks.domain.repository.TaskRepository
import com.app.officegrid.team.domain.repository.EmployeeRepository
import com.app.officegrid.core.common.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
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
                // 🚀 STEP 1: Wait for the absolute FIRST session signal from Auth
                // This replaces the old parallel check which was causing the race condition.
                authRepository.getCurrentUser()
                    .onStart { 
                        // Ensure we are in initializing state when starting
                        _sessionState.update { it.copy(isInitializing = true) }
                    }
                    .collect { user ->
                        if (user != null) {
                            _sessionState.update { currentState ->
                                SessionState(
                                    isInitializing = false,
                                    isLoggedIn = true,
                                    userRole = user.role,
                                    isApproved = if (currentState.activeCompanyId == user.companyId) user.isApproved else user.isApproved,
                                    activeCompanyId = currentState.activeCompanyId ?: user.companyId,
                                    userId = user.id
                                )
                            }
                        } else {
                            // Only set to false if we are sure there is no user
                            _sessionState.update { 
                                SessionState(isInitializing = false, isLoggedIn = false) 
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Session Initialization Failed")
                _sessionState.update { it.copy(isInitializing = false, isLoggedIn = false) }
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

    fun logout() {
        scope.launch(Dispatchers.IO) {
            try {
                Timber.d("LOGOUT: Initiating secure teardown...")
                authRepository.logout()
                taskRepository.get().clearLocalData()
                notificationRepository.get().clearAllNotifications()
                _sessionState.update { SessionState(isInitializing = false, isLoggedIn = false) }
                Timber.d("LOGOUT: Cleanup complete.")
            } catch (e: Exception) {
                Timber.e(e, "Logout error")
                _sessionState.update { SessionState(isInitializing = false, isLoggedIn = false) }
            }
        }
    }
}
