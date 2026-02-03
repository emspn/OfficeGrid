package com.app.officegrid.organization.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.model.EmployeeStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkspaceListUiState(
    val workspaces: List<Employee> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WorkspaceListViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceListUiState())
    val uiState: StateFlow<WorkspaceListUiState> = _uiState.asStateFlow()

    init {
        loadWorkspaces()
    }

    private fun loadWorkspaces() {
        val userId = sessionManager.sessionState.value.userId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            employeeRepository.getEmployeesByUserId(userId)
                .onEach { workspaces ->
                    _uiState.update { 
                        it.copy(workspaces = workspaces, isLoading = false) 
                    }
                }
                .catch { e ->
                    _uiState.update { 
                        it.copy(error = e.message, isLoading = false) 
                    }
                }
                .collect()
        }
    }

    fun selectWorkspace(workspace: Employee) {
        sessionManager.switchWorkspace(
            companyId = workspace.companyId,
            isApproved = workspace.status == EmployeeStatus.APPROVED
        )
    }
}
