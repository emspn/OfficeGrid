package com.app.officegrid.employee.presentation.workspace_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.team.domain.model.EmployeeStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sessionManager: SessionManager,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _workspaces = MutableStateFlow<UiState<List<WorkspaceItem>>>(UiState.Loading)
    val workspaces: StateFlow<UiState<List<WorkspaceItem>>> = _workspaces.asStateFlow()

    private val _events = MutableSharedFlow<WorkspaceEvent>()
    val events = _events.asSharedFlow()

    private var workspaceCollectionJob: Job? = null

    init {
        startWorkspaceObservation()
        refreshWorkspaces()
    }

    private fun startWorkspaceObservation() {
        workspaceCollectionJob?.cancel()
        workspaceCollectionJob = viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                if (user == null) {
                    _workspaces.value = UiState.Success(emptyList())
                } else {
                    employeeRepository.getEmployeesByUserId(user.id).collect { employees ->
                        val workspaceItems = employees.map { employee ->
                            val orgName = try {
                                employeeRepository.getOrganizationName(employee.companyId)
                            } catch (e: Exception) {
                                null
                            }

                            WorkspaceItem(
                                id = employee.id,
                                name = orgName ?: "Node: ${employee.companyId}",
                                companyId = employee.companyId,
                                taskCount = 0,
                                status = when (employee.status) {
                                    EmployeeStatus.APPROVED -> WorkspaceStatus.ACTIVE
                                    EmployeeStatus.PENDING -> WorkspaceStatus.PENDING
                                }
                            )
                        }
                        _workspaces.value = UiState.Success(workspaceItems)
                    }
                }
            }
        }
    }

    fun selectWorkspace(workspace: WorkspaceItem) {
        sessionManager.switchWorkspace(
            companyId = workspace.companyId,
            isApproved = workspace.status == WorkspaceStatus.ACTIVE
        )
    }

    fun leaveWorkspace(companyId: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first() ?: return@launch
            val result = employeeRepository.leaveWorkspace(user.id, companyId)
            if (result.isSuccess) {
                _events.emit(WorkspaceEvent.Success("Successfully left workspace."))
                refreshWorkspaces()
            } else {
                _events.emit(WorkspaceEvent.Error("Failed to leave workspace."))
            }
        }
    }

    fun refreshWorkspaces() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                employeeRepository.syncEmployeesByUserId(user.id)
            }
        }
    }

    fun joinWorkspace(code: String) {
        viewModelScope.launch {
            try {
                val normalizedCode = code.trim().uppercase()
                val user = getCurrentUserUseCase().first() ?: return@launch
                
                val result = employeeRepository.joinWorkspace(
                    userId = user.id,
                    userName = user.fullName,
                    userEmail = user.email,
                    companyId = normalizedCode
                )

                if (result.isSuccess) {
                    _events.emit(WorkspaceEvent.Success("Request sent! Waiting for approval."))
                    refreshWorkspaces()
                } else {
                    _events.emit(WorkspaceEvent.Error("Failed to join workspace."))
                }
            } catch (e: Exception) {
                _events.emit(WorkspaceEvent.Error("Network error."))
            }
        }
    }
}

sealed class WorkspaceEvent {
    data class Success(val message: String) : WorkspaceEvent()
    data class Error(val message: String) : WorkspaceEvent()
}

data class WorkspaceItem(
    val id: String,
    val name: String,
    val companyId: String,
    val taskCount: Int = 0,
    val status: WorkspaceStatus
)

enum class WorkspaceStatus {
    ACTIVE, PENDING
}
