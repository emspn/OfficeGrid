package com.app.officegrid.employee.presentation.task_update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskStatusUpdateViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _updateState = MutableStateFlow<TaskUpdateState>(TaskUpdateState.Idle)
    val updateState: StateFlow<TaskUpdateState> = _updateState.asStateFlow()

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            _updateState.value = TaskUpdateState.Loading

            android.util.Log.d("TaskStatusUpdate", "Updating task $taskId to status: $newStatus")

            try {
                val result = taskRepository.updateTaskStatus(taskId, newStatus)

                if (result.isSuccess) {
                    android.util.Log.d("TaskStatusUpdate", "Successfully updated task status")
                    _updateState.value = TaskUpdateState.Success(
                        message = when (newStatus) {
                            TaskStatus.IN_PROGRESS -> "Task started successfully!"
                            TaskStatus.DONE -> "Task completed! Great work! 🎉"
                            TaskStatus.TODO -> "Task reopened"
                        }
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Failed to update task status"
                    android.util.Log.e("TaskStatusUpdate", "Failed: $error")
                    _updateState.value = TaskUpdateState.Error(error)
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskStatusUpdate", "Exception: ${e.message}", e)
                _updateState.value = TaskUpdateState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _updateState.value = TaskUpdateState.Idle
    }
}

sealed class TaskUpdateState {
    object Idle : TaskUpdateState()
    object Loading : TaskUpdateState()
    data class Success(val message: String) : TaskUpdateState()
    data class Error(val message: String) : TaskUpdateState()
}
