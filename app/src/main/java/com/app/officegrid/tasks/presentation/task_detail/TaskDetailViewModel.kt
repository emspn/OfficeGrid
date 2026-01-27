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
import com.app.officegrid.tasks.domain.model.TaskRemark
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import com.app.officegrid.tasks.domain.repository.TaskRemarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
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
    
    private val _state = MutableStateFlow<UiState<Task>>(UiState.Loading)
    val state: StateFlow<UiState<Task>> = _state.asStateFlow()

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

    init {
        loadTask()
    }

    private fun loadTask() {
        val id = taskId ?: return
        viewModelScope.launch {
            _state.value = repository.getTaskById(id).toUiState()
            remarkRepository.syncRemarks(id)
        }
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
            
            // 1. Update the actual task status
            repository.updateTaskStatus(id, newStatus)
                .onSuccess {
                    // 2. Auto-log a system remark for the trail
                    remarkRepository.addTaskRemark(id, "SYSTEM_LOG: Status transition to ${newStatus.name}")
                    
                    _isUpdating.value = false
                    _events.send(UiEvent.ShowMessage("Workflow status updated"))
                    loadTask() 
                }
                .onFailure { error ->
                    _isUpdating.value = false
                    _events.send(UiEvent.ShowMessage(error.message ?: "Update failure"))
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
                    _events.send(UiEvent.ShowMessage("Task purged from system"))
                    _events.send(UiEvent.Navigate("back"))
                }
                .onFailure { error ->
                    _isUpdating.value = false
                    _events.send(UiEvent.ShowMessage(error.message ?: "Purge failure"))
                }
        }
    }
}