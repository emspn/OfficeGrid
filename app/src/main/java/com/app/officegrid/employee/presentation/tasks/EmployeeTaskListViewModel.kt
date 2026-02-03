package com.app.officegrid.employee.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EmployeeTaskListViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatus = MutableStateFlow<TaskStatus?>(null)
    val selectedStatus: StateFlow<TaskStatus?> = _selectedStatus.asStateFlow()

    private val _workspaceFilter = MutableStateFlow<String?>(null)

    // ⚡ REALTIME COMMUNICATION HUB
    val tasks: StateFlow<UiState<List<Task>>> = combine(
        getCurrentUserUseCase(),
        _workspaceFilter,
        _searchQuery,
        _selectedStatus
    ) { user, workspaceId, query, statusFilter ->
        if (user == null || workspaceId == null) {
            UiState.Loading
        } else {
            // Observe the repository which is now "Workspace Aware"
            taskRepository.getTasks(user.id).map { allTasks ->
                android.util.Log.d("EmployeeTaskListVM", "📊 Received ${allTasks.size} total tasks from repo")
                
                var filtered = allTasks.filter { it.companyId == workspaceId }
                
                // 🚀 HERO FIX: Ensure we are filtering by the CORRECT user ID
                // Check if any tasks exist for this user at all
                val userTasks = filtered.filter { it.assignedTo == user.id }
                android.util.Log.d("EmployeeTaskListVM", "   → Found ${userTasks.size} tasks assigned to ${user.id}")

                if (query.isNotBlank()) {
                    filtered = filtered.filter { it.title.contains(query, ignoreCase = true) }
                }
                if (statusFilter != null) {
                    filtered = filtered.filter { it.status == statusFilter }
                }
                
                UiState.Success(userTasks.sortedByDescending { it.createdAt })
            }.first()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    fun setWorkspaceFilter(workspaceId: String) {
        _workspaceFilter.value = workspaceId
        syncTasks()
    }

    fun syncTasks() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                android.util.Log.d("EmployeeTaskListVM", "🔄 Manually triggering sync for user ${user.id}")
                taskRepository.syncTasks(user.id)
            }
        }
    }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onStatusFilterChange(status: TaskStatus?) { _selectedStatus.value = status }
}
