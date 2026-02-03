package com.app.officegrid.team.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.notification.NotificationHelper
import com.app.officegrid.team.domain.model.EmployeeStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val repository: EmployeeRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _state = MutableStateFlow(TeamUiState())
    val state: StateFlow<TeamUiState> = _state.asStateFlow()

    init {
        observeTeam()
        // Initial sync
        syncTeam()
    }

    private fun observeTeam() {
        viewModelScope.launch {
            getCurrentUserUseCase().collectLatest { user ->
                if (user != null) {
                    repository.getEmployees(user.companyId).collect { employees ->
                        _state.update {
                            it.copy(
                                // Filter out the current admin from the list
                                pendingRequests = employees.filter { e -> e.status == EmployeeStatus.PENDING && e.id != user.id },
                                approvedMembers = employees.filter { e -> e.status == EmployeeStatus.APPROVED && e.id != user.id }
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
            // Don't set isLoading to true if we're just background syncing to avoid flicker
            repository.syncEmployees(user.companyId)
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    fun approveEmployee(employeeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Get employee info before approving
            val user = getCurrentUserUseCase().first()
            val employees = _state.value.pendingRequests + _state.value.approvedMembers
            val employee = employees.find { it.id == employeeId }

            repository.updateEmployeeStatus(employeeId, EmployeeStatus.APPROVED)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, successMessage = "✅ Team member approved successfully!") }

                    // ✅ Send notification to OPERATIVE that they've been approved
                    if (employee != null && user != null) {
                        notificationHelper.notifyJoinApproved(
                            employeeId = employeeId,
                            workspaceName = user.companyName ?: "workspace"
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "❌ Unable to approve. Please try again.") } }
        }
    }

    fun removeEmployee(employeeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Get employee info before removing
            val user = getCurrentUserUseCase().first()
            val employees = _state.value.pendingRequests + _state.value.approvedMembers
            val employee = employees.find { it.id == employeeId }

            repository.deleteEmployee(employeeId)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, successMessage = "Team member removed successfully") }

                    // ✅ Send notification to OPERATIVE that they've been rejected (if pending)
                    if (employee != null && employee.status == EmployeeStatus.PENDING && user != null) {
                        notificationHelper.notifyJoinRejected(
                            employeeId = employeeId,
                            workspaceName = user.companyName ?: "workspace"
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "❌ Unable to remove. Please try again.") } }
        }
    }

    fun updateRole(employeeId: String, newRole: String) {
        viewModelScope.launch {
            repository.updateEmployeeRole(employeeId, newRole)
                .onSuccess { _state.update { it.copy(successMessage = "✅ Role updated successfully!") } }
                .onFailure { e -> _state.update { it.copy(error = e.message ?: "❌ Unable to update role.") } }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(successMessage = null, error = null) }
    }
}
