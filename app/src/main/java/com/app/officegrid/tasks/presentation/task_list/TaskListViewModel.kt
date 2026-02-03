package com.app.officegrid.tasks.presentation.task_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiEvent
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.asUiState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskSortOption
import com.app.officegrid.tasks.domain.model.sortByOption
import com.app.officegrid.tasks.domain.usecase.GetTasksUseCase
import com.app.officegrid.tasks.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("unused")
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val repository: TaskRepository
) : ViewModel() {

    private val _events = Channel<UiEvent>()
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private val _selectedStatus = MutableStateFlow<TaskStatus?>(null)
    val selectedStatus: StateFlow<TaskStatus?> = _selectedStatus.asStateFlow()

    private val _selectedPriority = MutableStateFlow<TaskPriority?>(null)
    val selectedPriority: StateFlow<TaskPriority?> = _selectedPriority.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(TaskSortOption.DUE_DATE_ASC)
    val sortOption: StateFlow<TaskSortOption> = _sortOption.asStateFlow()

    val state: StateFlow<UiState<List<Task>>> = combine(
        getCurrentUserUseCase(),
        _selectedStatus,
        _selectedPriority,
        _searchQuery,
        _sortOption
    ) { user, status, priority, query, sortOption ->
        listOf(user, status, priority, query, sortOption)
    }.flatMapLatest { params ->
        val user = params[0] as com.app.officegrid.auth.domain.model.User?
        val status = params[1] as TaskStatus?
        val priority = params[2] as TaskPriority?
        val query = params[3] as String
        val sortOption = params[4] as TaskSortOption

        val userId = user?.id ?: "anonymous"
        getTasksUseCase(userId).map { tasks ->
            var filteredTasks = tasks

            // Filter by status
            if (status != null) {
                filteredTasks = filteredTasks.filter { it.status == status }
            }

            // Filter by priority
            if (priority != null) {
                filteredTasks = filteredTasks.filter { it.priority == priority }
            }

            // Filter by search query
            if (query.isNotBlank()) {
                filteredTasks = filteredTasks.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
                }
            }

            // Apply sorting
            filteredTasks.sortByOption(sortOption)
        }
    }.asUiState()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    init {
        // Auto-sync tasks on initialization
        syncTasks()
    }

    fun onStatusFilterSelected(status: TaskStatus?) {
        _selectedStatus.value = status
    }

    fun onPriorityFilterSelected(priority: TaskPriority?) {
        _selectedPriority.value = priority
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchClear() {
        _searchQuery.value = ""
    }

    fun onSortOptionSelected(sortOption: TaskSortOption) {
        _sortOption.value = sortOption
    }

    fun onTaskClick(taskId: String) {
        viewModelScope.launch {
            _events.send(UiEvent.Navigate("task_detail/$taskId"))
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
                .onSuccess {
                    _events.send(UiEvent.ShowMessage("Task deleted successfully"))
                }
                .onFailure { error ->
                    _events.send(UiEvent.ShowMessage(error.message ?: "Failed to delete task"))
                }
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, newStatus)
                .onSuccess {
                    _events.send(UiEvent.ShowMessage("Status updated"))
                }
                .onFailure { error ->
                    _events.send(UiEvent.ShowMessage(error.message ?: "Failed to update status"))
                }
        }
    }

    fun syncTasks() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first() ?: return@launch
            repository.syncTasks(user.id)
        }
    }
}

