package com.app.officegrid.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.asUiState
import com.app.officegrid.dashboard.domain.model.Analytics
import com.app.officegrid.dashboard.domain.usecase.GetAnalyticsUseCase
import com.app.officegrid.dashboard.domain.repository.AnalyticsRepository
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformanceItem(
    val employeeName: String,
    val totalTasks: Int,
    val completedTasks: Int
)

data class DashboardData(
    val analytics: Analytics,
    val performanceList: List<PerformanceItem>
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val employeeRepository: EmployeeRepository,
    private val repository: AnalyticsRepository
) : ViewModel() {

    val currentUser = getCurrentUserUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val state: StateFlow<UiState<DashboardData?>> = getCurrentUserUseCase()
        .flatMapLatest { user ->
            if (user == null) flowOf(UiState.Success(null))
            else {
                combine(
                    getAnalyticsUseCase(user.companyId),
                    employeeRepository.getEmployees(user.companyId)
                ) { analytics, employees ->
                    analytics?.let {
                        val performance = it.tasksPerEmployee.map { (empId, count) ->
                            val empName = employees.find { e -> e.id == empId }?.name ?: "Unknown"
                            PerformanceItem(
                                employeeName = empName,
                                totalTasks = count,
                                completedTasks = it.completedTasksPerEmployee[empId] ?: 0
                            )
                        }
                        DashboardData(it, performance)
                    }
                }.asUiState()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    init {
        syncData()
    }

    fun syncData() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.syncAnalytics(user.companyId)
        }
    }
}