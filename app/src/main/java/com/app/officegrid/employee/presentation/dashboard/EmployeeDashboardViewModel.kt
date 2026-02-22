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

    private val _workspaceFilter = MutableStateFlow<String?>(null)

    val dashboardData: StateFlow<UiState<EmployeeDashboardData>> = combine(
        getCurrentUserUseCase(),
        _workspaceFilter
    ) { user, workspaceId ->
        user to workspaceId
    }.flatMapLatest { (user, workspaceId) ->
            if (user == null) {
                // 🚀 PRODUCTION FIX: Show Loading while user session is initializing
                // Prevents the "User not found" error flash on dashboard entry
                flowOf(UiState.Loading)
            } else {
                getTasksUseCase(user.id).map { allTasks ->
                    var myTasks = allTasks.filter { it.assignedTo == user.id }

                    if (!workspaceId.isNullOrBlank()) {
                        myTasks = myTasks.filter { it.companyId == workspaceId }
                    }

                    val total = myTasks.size
                    val todo = myTasks.count { it.status == TaskStatus.TODO }
                    val inProgress = myTasks.count { it.status == TaskStatus.IN_PROGRESS }
                    val pendingApproval = myTasks.count { it.status == TaskStatus.PENDING_COMPLETION }
                    val completed = myTasks.count { it.status == TaskStatus.DONE }

                    val completionRate = if (total > 0) completed.toFloat() / total.toFloat() else 0f

                    val recentTasks = myTasks
                        .sortedBy { it.dueDate }
                        .take(5)

                    UiState.Success(
                        EmployeeDashboardData(
                            totalTasks = total,
                            todoTasks = todo,
                            inProgressTasks = inProgress,
                            pendingApprovalTasks = pendingApproval,
                            completedTasks = completed,
                            completionRate = completionRate,
                            recentTasks = recentTasks
                        )
                    ) as UiState<EmployeeDashboardData>
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
        syncTasks()
    }

    fun setWorkspaceFilter(workspaceId: String?) {
        _workspaceFilter.value = workspaceId
    }

    fun syncTasks() {
        viewModelScope.launch {
            try {
                // Wait for valid user session before syncing
                val user = getCurrentUserUseCase().filterNotNull().first()
                taskRepository.syncTasks(user.id)
            } catch (e: Exception) {
                android.util.Log.e("EmployeeDashboardVM", "❌ Sync error: ${e.message}")
            }
        }
    }
}

data class EmployeeDashboardData(
    val totalTasks: Int,
    val todoTasks: Int,
    val inProgressTasks: Int,
    val pendingApprovalTasks: Int,
    val completedTasks: Int,
    val completionRate: Float,
    val recentTasks: List<Task>
)
