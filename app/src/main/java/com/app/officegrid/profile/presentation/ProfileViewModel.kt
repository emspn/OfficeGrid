package com.app.officegrid.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.asUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfileData(val email: String, val role: String)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    val state: StateFlow<UiState<ProfileData?>> = getCurrentUserUseCase()
        .map { user ->
            user?.let {
                ProfileData(
                    email = it.email,
                    role = it.role.name
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
        sessionManager.logout()
    }
}