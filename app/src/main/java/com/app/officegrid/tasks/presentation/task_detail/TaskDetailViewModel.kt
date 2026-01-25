package com.app.officegrid.tasks.presentation.task_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiEvent
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.toUiState
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: String? = savedStateHandle["taskId"]
    
    private val _state = MutableStateFlow<UiState<Task>>(UiState.Loading)
    val state: StateFlow<UiState<Task>> = _state.asStateFlow()

    private val _events = Channel<UiEvent>()
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    val currentUser: Flow<User?> = getCurrentUserUseCase()

    init {
        loadTask()
    }

    private fun loadTask() {
        val id = taskId ?: return
        viewModelScope.launch {
            _state.value = repository.getTaskById(id).toUiState()
        }
    }

    fun updateStatus(newStatus: TaskStatus) {
        val id = taskId ?: return
        viewModelScope.launch {
            _isUpdating.value = true
            repository.updateTaskStatus(id, newStatus)
                .onSuccess {
                    _isUpdating.value = false
                    _events.send(UiEvent.ShowMessage("Status updated successfully"))
                    loadTask() // Refresh data
                }
                .onFailure { error ->
                    _isUpdating.value = false
                    _events.send(UiEvent.ShowMessage(error.message ?: "Failed to update status"))
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
                    _events.send(UiEvent.ShowMessage(error.message ?: "Failed to delete task"))
                }
        }
    }
}