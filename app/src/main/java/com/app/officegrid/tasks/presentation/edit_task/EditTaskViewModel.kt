package com.app.officegrid.tasks.presentation.edit_task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.Screen
import com.app.officegrid.core.ui.UiEvent
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.repository.TaskRepository
import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.model.EmployeeStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val taskId: String? = savedStateHandle["taskId"]

    private val _state = MutableStateFlow(EditTaskUiState())
    val state: StateFlow<EditTaskUiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>()
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _priority = MutableStateFlow(TaskPriority.MEDIUM)
    val priority: StateFlow<TaskPriority> = _priority.asStateFlow()

    private val _assignedTo = MutableStateFlow("")
    val assignedTo: StateFlow<String> = _assignedTo.asStateFlow()

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private var originalTask: Task? = null

    init {
        loadTaskAndEmployees()
    }

    // ⚡ NEW: Public function to reload employees (called on screen resume)
    fun refreshEmployees() {
        loadEmployees()
    }

    private fun loadTaskAndEmployees() {
        viewModelScope.launch {
            taskId?.let { id ->
                // Load task first
                repository.getTaskById(id).onSuccess { task ->
                    originalTask = task
                    _title.value = task.title
                    _description.value = task.description
                    _priority.value = task.priority
                    _assignedTo.value = task.assignedTo
                }.onFailure {
                    _events.send(UiEvent.ShowMessage("Failed to load task"))
                }
            }
            // Load employees
            loadEmployees()
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase().first() ?: return@launch
                android.util.Log.d("EditTaskViewModel", "⚡ Loading employees for company: ${user.companyId}")

                // 1. Sync from Supabase first
                employeeRepository.syncEmployees(user.companyId)

                // 2. Observe local database
                employeeRepository.getEmployees(user.companyId).collect { employees ->
                    _employees.value = employees.filter { it.status == EmployeeStatus.APPROVED }
                    android.util.Log.d("EditTaskViewModel", "✅ Loaded ${employees.size} employees")
                }
            } catch (e: Exception) {
                android.util.Log.e("EditTaskViewModel", "❌ Error loading employees: ${e.message}", e)
            }
        }
    }

    fun onTitleChange(value: String) { _title.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onPriorityChange(value: TaskPriority) { _priority.value = value }
    fun onAssignedToChange(value: String) { _assignedTo.value = value }

    fun updateTask() {
        if (_title.value.isBlank()) {
            viewModelScope.launch { _events.send(UiEvent.ShowMessage("⚠️ Please enter a task title")) }
            return
        }

        val currentTask = originalTask ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val updatedTask = currentTask.copy(
                title = _title.value,
                description = _description.value,
                priority = _priority.value,
                assignedTo = _assignedTo.value
            )

            repository.updateTask(updatedTask)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                    _events.send(UiEvent.ShowMessage("✅ Task updated successfully!"))
                    _events.send(UiEvent.Navigate(Screen.AdminTasks.route))
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                    _events.send(UiEvent.ShowMessage(error.message ?: "❌ Unable to update task. Please try again."))
                }
        }
    }
}
