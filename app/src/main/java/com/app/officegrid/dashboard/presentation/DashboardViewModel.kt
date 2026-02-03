package com.app.officegrid.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.dashboard.domain.model.Analytics
import com.app.officegrid.dashboard.domain.repository.AnalyticsRepository
import com.app.officegrid.tasks.domain.usecase.GetAllTasksUseCase
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformanceItem(
    val employeeId: String,
    val employeeName: String,
    val tasksAssigned: Int,
    val tasksCompleted: Int,
    val completionRate: Float
)

data class DashboardData(
    val analytics: Analytics,
    val teamPerformance: List<PerformanceItem>
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getAnalyticsUseCase: com.app.officegrid.dashboard.domain.usecase.GetAnalyticsUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val employeeRepository: EmployeeRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    init {
        syncData()
    }

    val currentUser = getCurrentUserUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    val state: StateFlow<UiState<DashboardData>> = combine(
        getCurrentUserUseCase(),
        _refreshTrigger.onStart { emit(Unit) }
    ) { user, _ -> user }
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(UiState.Error("User session not found"))
            } else {
                combine(
                    getAnalyticsUseCase(user.companyId),
                    getAllTasksUseCase(),
                    employeeRepository.getEmployees(user.companyId)
                ) { analytics, allTasks, employees ->
                    val analyticsObj = analytics ?: Analytics(0, 0, 0, 0, 0, emptyMap(), emptyMap())
                    
                    val performance = employees
                        .filter { it.id != user.id }
                        .map { employee ->
                            val assignedTasks = allTasks.filter { it.assignedTo == employee.id }
                            val completed = assignedTasks.count { it.status == TaskStatus.DONE }
                            val rate = if (assignedTasks.isNotEmpty()) completed.toFloat() / assignedTasks.size.toFloat() else 0f
                            
                            PerformanceItem(
                                employeeId = employee.id,
                                employeeName = employee.name,
                                tasksAssigned = assignedTasks.size,
                                tasksCompleted = completed,
                                completionRate = rate
                            )
                        }.sortedByDescending { it.completionRate }

                    val successState: UiState<DashboardData> = UiState.Success(DashboardData(analyticsObj, performance))
                    successState
                }.catch { e ->
                    val errorState: UiState<DashboardData> = UiState.Error(e.message ?: "Data stream interrupted")
                    emit(errorState)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun syncData() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            user?.let { 
                analyticsRepository.syncAnalytics(it.companyId)
                employeeRepository.syncEmployees(it.companyId)
                _refreshTrigger.emit(Unit)
            }
        }
    }
}
