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
        
        if (_email.value.isBlank() || _password.value.isBlank() || _fullName.value.isBlank() || _companyId.value.isBlank()) {
            _state.update { it.copy(error = "All mandatory fields are required") }
            return
        }
        
        if (isOrg && (_organisationName.value.isBlank() || _organisationType.value.isBlank())) {
            _state.update { it.copy(error = "Organisation details are required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.signup(
                email = _email.value,
                password = _password.value,
                fullName = _fullName.value,
                role = role,
                companyId = _companyId.value,
                companyName = if (isOrg) _organisationName.value else null,
                orgType = if (isOrg) _organisationType.value else null
            )
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}