package com.app.officegrid.team.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.model.EmployeeStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val repository: EmployeeRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TeamUiState())
    val state: StateFlow<TeamUiState> = _state.asStateFlow()

    init {
        observeTeam()
    }

    private fun observeTeam() {
        viewModelScope.launch {
            getCurrentUserUseCase().collectLatest { user ->
                if (user != null) {
                    repository.getEmployees(user.companyId).collect { employees ->
                        _state.update {
                            it.copy(
                                pendingRequests = employees.filter { e -> e.status == EmployeeStatus.PENDING },
                                approvedMembers = employees.filter { e -> e.status == EmployeeStatus.APPROVED }
                            )
                        }
                    }
                }
            }
        }
    }

    fun syncTeam() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first() ?: return@launch
            _state.update { it.copy(isLoading = true) }
            repository.syncEmployees(user.companyId)
                .onSuccess { _state.update { it.copy(isLoading = false) } }
                .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.message) } }
        }
    }

    fun approveEmployee(employeeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val result = repository.updateEmployeeStatus(employeeId, EmployeeStatus.APPROVED)
                if (result.isSuccess) {
                    _state.update { it.copy(isLoading = false, successMessage = "Employee approved successfully") }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to approve employee"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Unknown error occurred") }
            }
        }
    }

    fun rejectEmployee(employeeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val result = repository.deleteEmployee(employeeId)
                if (result.isSuccess) {
                    _state.update { it.copy(isLoading = false, successMessage = "Employee request rejected") }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to reject employee"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Unknown error occurred") }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(successMessage = null, error = null) }
    }
}