package com.app.officegrid.employee.presentation.dashboard

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
class EmployeeDashboardViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val dashboardData: StateFlow<UiState<EmployeeDashboardData>> = getCurrentUserUseCase()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(UiState.Error("User not found"))
            } else {
                getTasksUseCase(user.id).map { allTasks ->
                    // Filter only tasks assigned to this employee
                    val myTasks = allTasks.filter { it.assignedTo == user.id }

                    val total = myTasks.size
                    val todo = myTasks.count { it.status == TaskStatus.TODO }
                    val inProgress = myTasks.count { it.status == TaskStatus.IN_PROGRESS }
                    val completed = myTasks.count { it.status == TaskStatus.DONE }

                    val completionRate = if (total > 0) completed.toFloat() / total.toFloat() else 0f

                    // Recent tasks (last 5 sorted by due date)
                    val recentTasks = myTasks
                        .sortedBy { it.dueDate }
                        .take(5)

                    UiState.Success(
                        EmployeeDashboardData(
                            totalTasks = total,
                            todoTasks = todo,
                            inProgressTasks = inProgress,
                            completedTasks = completed,
                            completionRate = completionRate,
                            recentTasks = recentTasks
                        )
                    )
                }
            }
        }
        .catch { e -> emit(UiState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    init {
        // Auto-sync tasks on initialization
        syncTasks()
    }

    fun syncTasks() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first() ?: return@launch
            android.util.Log.d("EmployeeDashboard", "Syncing tasks for employee: ${user.id}")
            taskRepository.syncTasks(user.id)
        }
    }
}

data class EmployeeDashboardData(
    val totalTasks: Int,
    val todoTasks: Int,
    val inProgressTasks: Int,
    val completedTasks: Int,
    val completionRate: Float,
    val recentTasks: List<Task>
)
