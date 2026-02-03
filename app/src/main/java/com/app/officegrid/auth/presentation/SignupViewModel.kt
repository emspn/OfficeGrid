package com.app.officegrid.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignupUiState())
    val state: StateFlow<SignupUiState> = _state.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _organisationName = MutableStateFlow("")
    val organisationName: StateFlow<String> = _organisationName.asStateFlow()

    private val _organisationType = MutableStateFlow("")
    val organisationType: StateFlow<String> = _organisationType.asStateFlow()

    private val _companyId = MutableStateFlow("")
    val companyId: StateFlow<String> = _companyId.asStateFlow()

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onFullNameChange(value: String) { _fullName.value = value }
    fun onOrganisationNameChange(value: String) { _organisationName.value = value }
    fun onOrganisationTypeChange(value: String) { _organisationType.value = value }
    fun onCompanyIdChange(value: String) { 
        // Force uppercase and alpha-numeric for unique ID
        _companyId.value = value.uppercase().filter { it.isLetterOrDigit() } 
    }

    fun signup(role: UserRole) {
        val isOrg = role == UserRole.ADMIN
        
        // Better validation with clear error messages
        if (isOrg) {
            // ADMIN validation
            when {
                _email.value.isBlank() -> {
                    _state.update { it.copy(error = "Please enter your email address") }
                    return
                }
                !_email.value.contains("@") -> {
                    _state.update { it.copy(error = "Please enter a valid email address") }
                    return
                }
                _password.value.isBlank() -> {
                    _state.update { it.copy(error = "Please enter a password") }
                    return
                }
                _password.value.length < 8 -> {
                    _state.update { it.copy(error = "Password must be at least 8 characters") }
                    return
                }
                _companyId.value.isBlank() -> {
                    _state.update { it.copy(error = "Please enter a workspace ID") }
                    return
                }
                _companyId.value.length < 3 -> {
                    _state.update { it.copy(error = "Workspace ID must be at least 3 characters") }
                    return
                }
                _organisationName.value.isBlank() -> {
                    _state.update { it.copy(error = "Please enter your organization name") }
                    return
                }
                _organisationType.value.isBlank() -> {
                    _state.update { it.copy(error = "Please select organization type") }
                    return
                }
            }
        } else {
            // EMPLOYEE validation
            when {
                _fullName.value.isBlank() -> {
                    _state.update { it.copy(error = "Please enter your full name") }
                    return
                }
                _email.value.isBlank() -> {
                    _state.update { it.copy(error = "Please enter your email address") }
                    return
                }
                !_email.value.contains("@") -> {
                    _state.update { it.copy(error = "Please enter a valid email address") }
                    return
                }
                _password.value.isBlank() -> {
                    _state.update { it.copy(error = "Please enter a password") }
                    return
                }
                _password.value.length < 8 -> {
                    _state.update { it.copy(error = "Password must be at least 8 characters") }
                    return
                }
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // For ADMIN: use organization name as fullName
                val nameToUse = if (isOrg) _organisationName.value else _fullName.value

                authRepository.signup(
                    email = _email.value.trim(),
                    password = _password.value,
                    fullName = nameToUse.trim(),
                    role = role,
                    companyId = if (isOrg) _companyId.value else "",
                    companyName = if (isOrg) _organisationName.value.trim() else null,
                    orgType = if (isOrg) _organisationType.value else null
                )
                    .onSuccess {
                        _state.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                    .onFailure { error ->
                        // Final safety check: Filter out any URLs and "url" text that might have slipped through
                        val rawMessage = error.message ?: "Signup failed. Please try again."
                        android.util.Log.e("SignupViewModel", "Error received: $rawMessage")

                        val cleanMessage = if (rawMessage.contains("http", ignoreCase = true) ||
                                             rawMessage.contains("url", ignoreCase = true)) {
                            android.util.Log.e("SignupViewModel", "Message contains URL or 'url' text, filtering...")

                            val parts = rawMessage
                                .substringBefore("http", rawMessage)
                                .substringBefore("https", rawMessage)
                                .substringBefore("url", rawMessage)
                                .substringBefore("URL", rawMessage)
                                .trim()

                            if (parts.isNotBlank() && parts.length > 10) {
                                android.util.Log.e("SignupViewModel", "Using cleaned part: $parts")
                                parts.take(100)
                            } else {
                                android.util.Log.e("SignupViewModel", "No valid part, using fallback")
                                "Signup failed. Please check your connection and try again."
                            }
                        } else {
                            rawMessage.take(100) // Limit message length
                        }

                        android.util.Log.e("SignupViewModel", "Final message to user: $cleanMessage")
                        _state.update { it.copy(isLoading = false, error = cleanMessage) }
                    }
            } catch (e: Exception) {
                val errorMsg = "An error occurred. Please check your connection and try again."
                _state.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }
}
