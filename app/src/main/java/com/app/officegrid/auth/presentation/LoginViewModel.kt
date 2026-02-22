package com.app.officegrid.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(authResult = AuthResult.Error("Please enter both email and password.")) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(authResult = AuthResult.Loading) }
                
                val result = authRepository.login(
                    email = email,
                    password = password
                )

                result.onSuccess { session ->
                    // We trust the SessionManager and MainActivity to handle routing
                    _uiState.update { it.copy(authResult = AuthResult.Success) }
                }.onFailure { error ->
                    Timber.e(error, "Login failed")
                    _uiState.update { it.copy(authResult = AuthResult.Error(error.message ?: "Login failed. Please try again.")) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error during login")
                _uiState.update { it.copy(authResult = AuthResult.Error("An unexpected error occurred.")) }
            }
        }
    }
}
