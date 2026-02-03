package com.app.officegrid.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.asUiState
import com.app.officegrid.tasks.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileData(
    val fullName: String,
    val email: String, 
    val role: String,
    val companyId: String,
    val companyName: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val state: StateFlow<UiState<ProfileData?>> = getCurrentUserUseCase()
        .map { user ->
            user?.let {
                ProfileData(
                    fullName = it.fullName,
                    email = it.email,
                    role = it.role.name,
                    companyId = it.companyId,
                    companyName = it.companyName
                )
            }
        }
        .asUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun logout() {
        viewModelScope.launch {
            // Clear local task data before logging out
            taskRepository.clearLocalData()
            authRepository.logout()
        }
    }
}
