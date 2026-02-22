package com.app.officegrid.employee.presentation.workspace_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.notification.NotificationHelper
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.team.domain.model.EmployeeStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sessionManager: SessionManager,
    private val notificationHelper: NotificationHelper,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _events = MutableSharedFlow<WorkspaceEvent>()
    val events = _events.asSharedFlow()

    // ✅ CENTRALIZED UI STATE
    private val _showJoinDialog = MutableStateFlow(false)
    val showJoinDialog = _showJoinDialog.asStateFlow()

    private val _isJoining = MutableStateFlow(false)
    val isJoining = _isJoining.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _optimisticRemovals = MutableStateFlow<Set<String>>(emptySet())

    val workspaces: StateFlow<UiState<List<WorkspaceItem>>> = getCurrentUserUseCase()
        .distinctUntilChangedBy { it?.id } // Prevents flicker on metadata updates
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else employeeRepository.getEmployeesByUserId(user.id)
        }
        .combine(_optimisticRemovals) { employees, removals ->
            employees.filter { it.companyId !in removals }.map { employee ->
                WorkspaceItem(
                    id = employee.id,
                    name = employee.companyName ?: "Workspace: ${employee.companyId}",
                    companyId = employee.companyId,
                    taskCount = 0,
                    status = when (employee.status) {
                        EmployeeStatus.APPROVED -> WorkspaceStatus.ACTIVE
                        EmployeeStatus.PENDING -> WorkspaceStatus.PENDING
                    }
                )
            }
        }
        .map { UiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun toggleJoinDialog(show: Boolean) {
        _showJoinDialog.value = show
    }

    fun selectWorkspace(workspace: WorkspaceItem) {
        sessionManager.switchWorkspace(workspace.companyId, workspace.status == WorkspaceStatus.ACTIVE)
    }

    fun leaveWorkspace(companyId: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first() ?: return@launch
            _optimisticRemovals.update { it + companyId }
            
            val result = employeeRepository.leaveWorkspace(user.id, companyId)
            if (result.isFailure) {
                _optimisticRemovals.update { it - companyId }
                _events.emit(WorkspaceEvent.Error("Disconnect failed. Try again."))
            } else {
                _events.emit(WorkspaceEvent.Success("Node disconnected."))
            }
        }
    }

    fun refreshWorkspaces() {
        viewModelScope.launch {
            _isRefreshing.value = true
            getCurrentUserUseCase().first()?.let { employeeRepository.syncEmployeesByUserId(it.id) }
            _isRefreshing.value = false
        }
    }

    fun joinWorkspace(code: String) {
        if (_isJoining.value) return 
        viewModelScope.launch {
            _isJoining.value = true
            try {
                val user = getCurrentUserUseCase().first() ?: throw Exception("Session expired")
                val result = employeeRepository.joinWorkspace(user.id, user.fullName, user.email, code.trim().uppercase())

                if (result.isSuccess) {
                    _events.emit(WorkspaceEvent.Success("Request sent!"))
                    _showJoinDialog.value = false // ✅ CLOSE DIALOG HERE
                    employeeRepository.syncEmployeesByUserId(user.id)
                } else {
                    _events.emit(WorkspaceEvent.Error(result.exceptionOrNull()?.message ?: "Join failed"))
                }
            } catch (e: Exception) {
                _events.emit(WorkspaceEvent.Error(e.message ?: "Error"))
            } finally {
                _isJoining.value = false
            }
        }
    }
}

sealed class WorkspaceEvent {
    data class Success(val message: String) : WorkspaceEvent()
    data class Error(val message: String) : WorkspaceEvent()
}

data class WorkspaceItem(val id: String, val name: String, val companyId: String, val taskCount: Int = 0, val status: WorkspaceStatus)
enum class WorkspaceStatus { ACTIVE, PENDING }
