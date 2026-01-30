package com.app.officegrid.tasks.presentation.create_task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.Screen
import com.app.officegrid.core.ui.UiEvent
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.model.TaskTemplate
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val employeeRepository: EmployeeRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTaskUiState())
    val state: StateFlow<CreateTaskUiState> = _state.asStateFlow()

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

    private val _dueDate = MutableStateFlow(System.currentTimeMillis() + 86400000) // Default: tomorrow
    val dueDate: StateFlow<Long> = _dueDate.asStateFlow()

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    init {
        loadEmployees()
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first() ?: return@launch
            employeeRepository.getEmployees(user.companyId).collect { list ->
                _employees.value = list.filter { it.status == EmployeeStatus.APPROVED }
            }
        }
    }

    fun onTitleChange(value: String) { _title.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onPriorityChange(value: TaskPriority) { _priority.value = value }
    fun onAssignedToChange(value: String) { _assignedTo.value = value }
    fun onDueDateChange(value: Long) { _dueDate.value = value }

    fun createTask() {
        if (_title.value.isBlank()) {
            viewModelScope.launch { _events.send(UiEvent.ShowMessage("Title cannot be empty")) }
            return
        }

        if (_assignedTo.value.isBlank()) {
            viewModelScope.launch { _events.send(UiEvent.ShowMessage("Please assign the task to someone")) }
            return
        }

        viewModelScope.launch {
            val user = getCurrentUserUseCase().first() ?: return@launch
            
            _state.update { it.copy(isLoading = true, error = null) }
            
            val newTask = Task(
                id = UUID.randomUUID().toString(),
                title = _title.value,
                description = _description.value,
                status = TaskStatus.TODO,
                priority = _priority.value,
                assignedTo = _assignedTo.value,
                createdBy = user.id,
                companyId = user.companyId,
                dueDate = _dueDate.value
            )

            repository.createTask(newTask)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                    _events.send(UiEvent.ShowMessage("Task created successfully"))
                    _events.send(UiEvent.Navigate(Screen.AdminTasks.route))
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                    _events.send(UiEvent.ShowMessage(error.message ?: "Failed to create task"))
                }
        }
    }

    fun createTasksFromTemplate(template: TaskTemplate) {
        if (_assignedTo.value.isBlank()) {
            viewModelScope.launch {
                _events.send(UiEvent.ShowMessage("Please select an employee first"))
            }
            return
        }

        viewModelScope.launch {
            val user = getCurrentUserUseCase().first() ?: return@launch

            _state.update { it.copy(isLoading = true, error = null) }

            var successCount = 0
            var failCount = 0

            // Create tasks from template
            template.tasks.forEachIndexed { index, templateTask ->
                // Calculate due date based on estimated days
                val daysFromNow = template.tasks.take(index + 1).sumOf { it.estimatedDays }
                val taskDueDate = System.currentTimeMillis() + (daysFromNow * 86400000L)

                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = templateTask.title,
                    description = templateTask.description,
                    status = TaskStatus.TODO,
                    priority = templateTask.priority,
                    assignedTo = _assignedTo.value,
                    createdBy = user.id,
                    companyId = user.companyId,
                    dueDate = taskDueDate
                )

                repository.createTask(newTask)
                    .onSuccess { successCount++ }
                    .onFailure { failCount++ }
            }

            _state.update { it.copy(isLoading = false, isSuccess = successCount > 0) }

            if (successCount > 0) {
                _events.send(UiEvent.ShowMessage("✅ Created $successCount tasks from ${template.name}!"))
                _events.send(UiEvent.Navigate(Screen.AdminTasks.route))
            } else {
                _events.send(UiEvent.ShowMessage("❌ Failed to create tasks"))
            }
        }
    }
}

