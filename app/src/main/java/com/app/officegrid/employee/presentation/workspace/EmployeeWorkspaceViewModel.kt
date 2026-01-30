package com.app.officegrid.employee.presentation.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeWorkspaceViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    private val _userName = MutableStateFlow("Employee")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserInfo()
        loadWorkspaces()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _userName.value = user?.fullName ?: "Employee"
            }
        }
    }

    private fun loadWorkspaces() {
        viewModelScope.launch {
            // TODO: Load from repository
            // For now, using mock data
            _workspaces.value = listOf(
                // Will be populated from database
            )
        }
    }

    fun joinWorkspace(workspaceCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // TODO: Implement join workspace logic
                // 1. Validate code
                // 2. Create employee record with pending status
                // 3. Wait for admin approval

                // Mock success
                kotlinx.coroutines.delay(1000)
                _isLoading.value = false
                loadWorkspaces()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to join workspace"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
