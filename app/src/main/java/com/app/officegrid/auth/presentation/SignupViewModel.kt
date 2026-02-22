package com.app.officegrid.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) { _uiState.update { it.copy(email = value) } }
    fun onPasswordChange(value: String) { _uiState.update { it.copy(password = value) } }
    fun onFullNameChange(value: String) { _uiState.update { it.copy(fullName = value) } }
    fun onOrganisationNameChange(value: String) { _uiState.update { it.copy(organisationName = value) } }
    fun onOrganisationTypeChange(value: String) { _uiState.update { it.copy(organisationType = value) } }
    fun onCompanyIdChange(value: String) { 
        val formatted = value.uppercase().filter { it.isLetterOrDigit() }
        _uiState.update { it.copy(companyId = formatted) } 
    }

    fun signup(role: UserRole) {
        val state = _uiState.value
        val isOrg = role == UserRole.ADMIN
        
        val error = when {
            state.email.isBlank() || !state.email.contains("@") -> "Enter a valid email address"
            state.password.length < 8 -> "Password must be at least 8 characters"
            !isOrg && state.fullName.isBlank() -> "Please enter your full name"
            isOrg && state.companyId.length < 3 -> "Workspace ID must be at least 3 characters"
            isOrg && state.organisationName.isBlank() -> "Enter organization name"
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(authResult = AuthResult.Error(error)) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(authResult = AuthResult.Loading) }

                val result = authRepository.signup(
                    email = state.email.trim(),
                    password = state.password,
                    fullName = (if (isOrg) state.organisationName else state.fullName).trim(),
                    role = role,
                    companyId = if (isOrg) state.companyId else "",
                    companyName = if (isOrg) state.organisationName.trim() else null,
                    orgType = if (isOrg) state.organisationType else null
                )

                result.onSuccess { user ->
                    Timber.d("Signup success for role: ${role.name}. Approved: ${user.isApproved}")
                    if (user.isApproved) {
                        _uiState.update { it.copy(authResult = AuthResult.Success) }
                    } else {
                        _uiState.update { it.copy(authResult = AuthResult.NotApproved) }
                    }
                }.onFailure { e ->
                    Timber.e(e, "Signup failed")
                    // ✅ HERO FIX: Show the REAL error from the repository
                    _uiState.update { it.copy(authResult = AuthResult.Error(e.message ?: "Signup failed")) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(authResult = AuthResult.Error("System Error: ${e.localizedMessage}")) }
            }
        }
    }
}
