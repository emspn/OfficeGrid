package com.app.officegrid.tasks.presentation.task_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.model.User
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
import java.util.Calendar
import javax.inject.Inject

enum class TaskTimelineFilter {
    ALL, TODAY, THIS_WEEK, MONTHLY, YEARLY, CUSTOM
}

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

    // ✅ Default to MONTHLY as requested
    private val _timelineFilter = MutableStateFlow(TaskTimelineFilter.MONTHLY)
    val timelineFilter: StateFlow<TaskTimelineFilter> = _timelineFilter.asStateFlow()

    private val _dateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val dateRange: StateFlow<Pair<Long, Long>?> = _dateRange.asStateFlow()

    val currentUser: StateFlow<User?> = getCurrentUserUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val state: StateFlow<UiState<List<Task>>> = combine(
        currentUser,
        _selectedStatus,
        _selectedPriority,
        _searchQuery,
        _sortOption,
        _timelineFilter,
        _dateRange
    ) { args: Array<Any?> ->
        FilterParams(
            user = args[0] as User?,
            status = args[1] as TaskStatus?,
            priority = args[2] as TaskPriority?,
            query = args[3] as String,
            sortOption = args[4] as TaskSortOption,
            timeline = args[5] as TaskTimelineFilter,
            range = @Suppress("UNCHECKED_CAST") (args[6] as Pair<Long, Long>?)
        )
    }.flatMapLatest { params ->
        val userId = params.user?.id ?: "anonymous"
        getTasksUseCase(userId).map { tasks ->
            var filteredTasks = tasks

            if (params.status != null) {
                filteredTasks = filteredTasks.filter { it.status == params.status }
            }

            if (params.priority != null) {
                filteredTasks = filteredTasks.filter { it.priority == params.priority }
            }

            filteredTasks = when (params.timeline) {
                TaskTimelineFilter.ALL -> filteredTasks
                TaskTimelineFilter.TODAY -> filterByDays(filteredTasks, 0)
                TaskTimelineFilter.THIS_WEEK -> filterByDays(filteredTasks, 7)
                TaskTimelineFilter.MONTHLY -> filterByDays(filteredTasks, 30)
                TaskTimelineFilter.YEARLY -> filterByDays(filteredTasks, 365)
                TaskTimelineFilter.CUSTOM -> {
                    val range = params.range
                    if (range != null) {
                        filteredTasks.filter { it.dueDate in range.first..range.second }
                    } else filteredTasks
                }
            }

            if (params.query.isNotBlank()) {
                filteredTasks = filteredTasks.filter {
                    it.title.contains(params.query, ignoreCase = true) ||
                    it.description.contains(params.query, ignoreCase = true)
                }
            }

            filteredTasks.sortByOption(params.sortOption)
        }
    }.asUiState()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    private fun filterByDays(tasks: List<Task>, days: Int): List<Task> {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        return if (days == 0) {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val endOfDay = startOfDay + 86400000
            tasks.filter { it.dueDate in startOfDay..endOfDay }
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, days)
            val end = calendar.timeInMillis
            tasks.filter { it.dueDate in now..end }
        }
    }

    init {
        syncTasks()
    }

    fun onTimelineFilterSelected(filter: TaskTimelineFilter) {
        _timelineFilter.value = filter
    }

    fun onDateRangeSelected(start: Long, end: Long) {
        _dateRange.value = start to end
        _timelineFilter.value = TaskTimelineFilter.CUSTOM
    }

    fun onStatusFilterSelected(status: TaskStatus?) {
        _selectedStatus.value = status
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

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, newStatus)
                .onSuccess { _events.send(UiEvent.ShowMessage("Status updated")) }
                .onFailure { _events.send(UiEvent.ShowMessage(it.message ?: "Sync failed")) }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
                .onSuccess { _events.send(UiEvent.ShowMessage("Unit removed")) }
                .onFailure { _events.send(UiEvent.ShowMessage("Deletion failed")) }
        }
    }

    fun syncTasks() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.syncTasks(user.id)
        }
    }

    private data class FilterParams(
        val user: User?,
        val status: TaskStatus?,
        val priority: TaskPriority?,
        val query: String,
        val sortOption: TaskSortOption,
        val timeline: TaskTimelineFilter,
        val range: Pair<Long, Long>?
    )
}
