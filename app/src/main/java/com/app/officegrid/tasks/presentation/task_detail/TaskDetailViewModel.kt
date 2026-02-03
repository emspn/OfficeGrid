package com.app.officegrid.tasks.presentation.task_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiEvent
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskRemark
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import com.app.officegrid.tasks.domain.repository.TaskRemarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val remarkRepository: TaskRemarkRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: String? = savedStateHandle["taskId"]
    
    init {
        // Start periodic sync for remarks
        startPeriodicRemarkSync()
    }

    private fun startPeriodicRemarkSync() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(3000) // Sync every 3 seconds
                taskId?.let { id ->
                    try {
                        remarkRepository.syncRemarks(id)
                    } catch (e: Exception) {
                        android.util.Log.e("TaskDetailVM", "Remark sync failed: ${e.message}")
                    }
                }
            }
        }
    }

    // ✨ Observe the task in REAL-TIME from the database Flow
    val state: StateFlow<UiState<Task>> = if (taskId != null) {
        repository.observeTaskById(taskId)
            .map { task ->
                if (task != null) UiState.Success(task) else UiState.Error("Task not found")
            }
            .onStart { 
                // Trigger a sync when we start observing
                remarkRepository.syncRemarks(taskId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )
    } else {
        MutableStateFlow(UiState.Error("Invalid Task ID"))
    }

    private val _events = Channel<UiEvent>()
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _remarkMessage = MutableStateFlow("")
    val remarkMessage: StateFlow<String> = _remarkMessage.asStateFlow()

    val currentUser: Flow<User?> = getCurrentUserUseCase()

    val remarks: Flow<List<TaskRemark>> = if (taskId != null) {
        remarkRepository.getTaskRemarks(taskId)
    } else {
        flowOf(emptyList())
    }

    fun onRemarkMessageChange(message: String) {
        _remarkMessage.value = message
    }

    fun addRemark() {
        val id = taskId ?: return
        val message = _remarkMessage.value
        if (message.isBlank()) return

        viewModelScope.launch {
            _isUpdating.value = true
            remarkRepository.addTaskRemark(id, message)
                .onSuccess {
                    _isUpdating.value = false
                    _remarkMessage.value = ""
                    _events.send(UiEvent.ShowMessage("Log entry synchronized"))
                }
                .onFailure { error ->
                    _isUpdating.value = false
                    _events.send(UiEvent.ShowMessage(error.message ?: "Sync failure"))
                }
        }
    }

    fun updateStatus(newStatus: TaskStatus) {
        val id = taskId ?: return
        viewModelScope.launch {
            _isUpdating.value = true
            
            // Update status - Flow will automatically emit the new state
            repository.updateTaskStatus(id, newStatus)
                .onSuccess {
                    remarkRepository.addTaskRemark(id, "Status changed to ${newStatus.name.replace("_", " ")}")
                    _isUpdating.value = false
                    val message = when (newStatus) {
                        TaskStatus.TODO -> "Status updated to Pending"
                        TaskStatus.IN_PROGRESS -> "Status updated to In Progress"
                        TaskStatus.PENDING_COMPLETION -> "Status updated to Pending Review"
                        TaskStatus.DONE -> "Task marked as Completed"
                    }
                    _events.send(UiEvent.ShowMessage(message))
                }
                .onFailure { error ->
                    _isUpdating.value = false
                    _events.send(UiEvent.ShowMessage(error.message ?: "Unable to update status. Please try again."))
                }
        }
    }

    fun deleteTask() {
        val id = taskId ?: return
        viewModelScope.launch {
            _isUpdating.value = true
            repository.deleteTask(id)
                .onSuccess {
                    _isUpdating.value = false
                    _events.send(UiEvent.ShowMessage("Task deleted successfully"))
                    _events.send(UiEvent.Navigate("back"))
                }
                .onFailure { error ->
                    _isUpdating.value = false
                    _events.send(UiEvent.ShowMessage(error.message ?: "Unable to delete task. Please try again."))
                }
        }
    }

    // ⚡ NEW: Public function to refresh task and remarks
    fun refreshTaskAndRemarks() {
        viewModelScope.launch {
            taskId?.let { id ->
                android.util.Log.d("TaskDetailViewModel", "⚡ Refreshing task and remarks for: $id")
                // Sync task from Supabase
                repository.getTaskById(id)
                // Sync remarks from Supabase
                remarkRepository.syncRemarks(id)
            }
        }
    }
}
