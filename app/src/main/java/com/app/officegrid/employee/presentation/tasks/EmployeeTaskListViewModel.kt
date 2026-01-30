package com.app.officegrid.employee.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.usecase.GetTasksUseCase
import com.app.officegrid.tasks.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EmployeeTaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatus = MutableStateFlow<TaskStatus?>(null)
    val selectedStatus: StateFlow<TaskStatus?> = _selectedStatus.asStateFlow()

    val tasks: StateFlow<UiState<List<Task>>> = getCurrentUserUseCase()
        .flatMapLatest { user ->
            android.util.Log.d("EmployeeTaskList", "User in flow: ${user?.email}, ID: ${user?.id}")
            if (user == null) {
                android.util.Log.e("EmployeeTaskList", "User is null!")
                flowOf(UiState.Error("User not found"))
            } else {
                combine(
                    getTasksUseCase(user.id),
                    _searchQuery,
                    _selectedStatus
                ) { allTasks, query, statusFilter ->
                    android.util.Log.d("EmployeeTaskList", "All tasks count: ${allTasks.size}")
                    allTasks.forEach { task ->
                        android.util.Log.d("EmployeeTaskList", "Task: ${task.title}, assignedTo: ${task.assignedTo}, currentUser: ${user.id}")
                    }

                    // Filter: Only tasks assigned to this employee
                    val myTasks = allTasks.filter { it.assignedTo == user.id }
                    android.util.Log.d("EmployeeTaskList", "Filtered my tasks count: ${myTasks.size}")

                    // Apply search filter
                    val searchFiltered = if (query.isBlank()) {
                        myTasks
                    } else {
                        myTasks.filter {
                            it.title.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true)
                        }
                    }
                    
                    // Apply status filter
                    val statusFiltered = if (statusFilter == null) {
                        searchFiltered
                    } else {
                        searchFiltered.filter { it.status == statusFilter }
                    }
                    
                    android.util.Log.d("EmployeeTaskList", "Final tasks count: ${statusFiltered.size}")
                    UiState.Success(statusFiltered)
                }
            }
        }
        .catch { e ->
            android.util.Log.e("EmployeeTaskList", "Error in tasks flow: ${e.message}", e)
            emit(UiState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    init {
        // Auto-sync tasks on initialization
        android.util.Log.d("EmployeeTaskList", "ViewModel initialized, syncing tasks...")
        syncTasks()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChange(status: TaskStatus?) {
        _selectedStatus.value = status
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun syncTasks() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first() ?: return@launch
            android.util.Log.d("EmployeeTaskList", "Syncing tasks for user: ${user.id}")
            taskRepository.syncTasks(user.id)
        }
    }
}
