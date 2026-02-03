package com.app.officegrid.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.usecase.GetTasksUseCase
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    val analyticsData: StateFlow<UiState<AnalyticsData>> = getCurrentUserUseCase()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(UiState.Error("User not found"))
            } else {
                combine(
                    getTasksUseCase(user.id),
                    employeeRepository.getEmployees(user.companyId)
                ) { tasks, employees ->
                    val total = tasks.size
                    val completed = tasks.count { it.status == TaskStatus.DONE }
                    val inProgress = tasks.count { it.status == TaskStatus.IN_PROGRESS }
                    val todo = tasks.count { it.status == TaskStatus.TODO }

                    val completionRate = if (total > 0) completed.toFloat() / total.toFloat() else 0f

                    // Calculate average completion time (placeholder)
                    val avgTime = 2.4

                    // Priority breakdown
                    val highPriority = tasks.count { it.priority == TaskPriority.HIGH }
                    val mediumPriority = tasks.count { it.priority == TaskPriority.MEDIUM }
                    val lowPriority = tasks.count { it.priority == TaskPriority.LOW }

                    // Team performance
                    val teamPerformance = employees.map { employee ->
                        val assignedTasks = tasks.filter { it.assignedTo == employee.id }
                        val completedByEmployee = assignedTasks.count { it.status == TaskStatus.DONE }
                        val rate = if (assignedTasks.isNotEmpty()) {
                            completedByEmployee.toFloat() / assignedTasks.size.toFloat()
                        } else 0f

                        TeamMemberPerformance(
                            name = employee.name,
                            tasksCompleted = completedByEmployee,
                            tasksAssigned = assignedTasks.size,
                            completionRate = rate
                        )
                    }

                    UiState.Success(
                        AnalyticsData(
                            totalTasks = total,
                            completedTasks = completed,
                            inProgressTasks = inProgress,
                            todoTasks = todo,
                            completionRate = completionRate,
                            avgCompletionTime = avgTime,
                            highPriorityTasks = highPriority,
                            mediumPriorityTasks = mediumPriority,
                            lowPriorityTasks = lowPriority,
                            teamPerformance = teamPerformance
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
}

data class AnalyticsData(
    val totalTasks: Int,
    val completedTasks: Int,
    val inProgressTasks: Int,
    val todoTasks: Int,
    val completionRate: Float,
    val avgCompletionTime: Double,
    val highPriorityTasks: Int,
    val mediumPriorityTasks: Int,
    val lowPriorityTasks: Int,
    val teamPerformance: List<TeamMemberPerformance>
)

data class TeamMemberPerformance(
    val name: String,
    val tasksCompleted: Int,
    val tasksAssigned: Int,
    val completionRate: Float
)

