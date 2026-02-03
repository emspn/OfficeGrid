package com.app.officegrid.employee.presentation.workspace_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkspaceDetailViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _workspaceName = MutableStateFlow<String?>(null)
    val workspaceName: StateFlow<String?> = _workspaceName.asStateFlow()

    fun loadWorkspaceName(workspaceId: String) {
        // ✅ CRITICAL SAFETY: Ensure session knows this workspace is active
        // This ensures the TaskRepository starts its realtime stream for this company
        if (sessionManager.sessionState.value.activeCompanyId != workspaceId) {
            sessionManager.switchWorkspace(workspaceId, true)
        }

        viewModelScope.launch {
            try {
                val name = employeeRepository.getOrganizationName(workspaceId)
                _workspaceName.value = name
            } catch (e: Exception) {
                _workspaceName.value = null
            }
        }
    }
}
