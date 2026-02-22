package com.app.officegrid.team.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.model.EmployeeStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamViewModel @Inject constructor(
    private val repository: EmployeeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _successMessage = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    
    // For Optimistic UI updates
    private val _optimisticUpdates = MutableStateFlow<Map<String, EmployeeStatus?>>(emptyMap())

    val state: StateFlow<TeamUiState> = sessionManager.sessionState
        .flatMapLatest { session ->
            if (session.activeCompanyId != null) {
                repository.getEmployees(session.activeCompanyId)
            } else {
                flowOf(emptyList())
            }
        }
        .combine(_optimisticUpdates) { employees, optimistic ->
            employees.mapNotNull { employee ->
                val optimisticStatus = optimistic[employee.id]
                if (optimisticStatus == null && optimistic.containsKey(employee.id)) {
                    null
                } else if (optimisticStatus != null) {
                    employee.copy(status = optimisticStatus)
                } else {
                    employee
                }
            }
        }
        .combine(_isLoading) { employees, loading ->
            employees to loading
        }
        .combine(sessionManager.sessionState) { (employees, loading), session ->
            TeamUiState(
                approvedMembers = employees.filter { it.status == EmployeeStatus.APPROVED },
                pendingRequests = employees.filter { it.status == EmployeeStatus.PENDING },
                isLoading = loading,
                currentUserId = session.userId
            )
        }
        .combine(_successMessage) { state, msg -> state.copy(successMessage = msg) }
        .combine(_error) { state, err -> state.copy(error = err) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TeamUiState(isLoading = true)
        )

    init {
        syncTeam()
    }

    fun syncTeam() {
        val companyId = sessionManager.sessionState.value.activeCompanyId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repository.syncEmployees(companyId)
                .onFailure { _error.value = it.localizedMessage }
            _isLoading.value = false
        }
    }

    fun approveEmployee(employeeId: String) {
        viewModelScope.launch {
            _optimisticUpdates.update { it + (employeeId to EmployeeStatus.APPROVED) }
            
            repository.updateEmployeeStatus(employeeId, EmployeeStatus.APPROVED)
                .onSuccess { 
                    _successMessage.value = "Employee approved successfully"
                    _optimisticUpdates.update { it - employeeId }
                }
                .onFailure { 
                    _error.value = it.localizedMessage
                    _optimisticUpdates.update { it - employeeId }
                }
        }
    }

    fun updateRole(employeeId: String, newRole: String) {
        viewModelScope.launch {
            repository.updateEmployeeRole(employeeId, newRole)
                .onSuccess { _successMessage.value = "Role updated to $newRole" }
                .onFailure { _error.value = it.localizedMessage }
        }
    }

    fun removeEmployee(employeeId: String) {
        viewModelScope.launch {
            _optimisticUpdates.update { it + (employeeId to null) }
            
            repository.deleteEmployee(employeeId)
                .onSuccess { 
                    _successMessage.value = "Removed from workspace"
                    _optimisticUpdates.update { it - employeeId }
                }
                .onFailure { 
                    _error.value = it.localizedMessage
                    _optimisticUpdates.update { it - employeeId }
                }
        }
    }

    fun clearMessages() {
        _successMessage.value = null
        _error.value = null
    }
}
