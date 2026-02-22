package com.app.officegrid.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.dashboard.domain.model.Analytics
import com.app.officegrid.dashboard.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    val currentUser: StateFlow<User?> = getCurrentUserUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val state: StateFlow<UiState<DashboardData>> = currentUser
        .flatMapLatest { user ->
            if (user == null) {
                // 🚀 PRODUCTION FIX: Don't show error if user is still being loaded
                // This prevents the "Retry" flash on dashboard entry
                flowOf(UiState.Loading)
            } else {
                combine(
                    analyticsRepository.getAnalytics(user.companyId),
                    flow {
                        _refreshTrigger.onStart { emit(Unit) }.collect {
                            val result = analyticsRepository.getTeamPerformance(user.companyId)
                            emit(result.getOrDefault(emptyList()))
                        }
                    }
                ) { analytics, performance ->
                    val analyticsObj = analytics ?: Analytics()
                    // ✅ FIXED: Filter out the current user (Admin) from the performance list
                    val filteredPerformance = performance.filter { it.employeeId != user.id }
                    UiState.Success(DashboardData(analyticsObj, filteredPerformance)) as UiState<DashboardData>
                }.catch { e ->
                    // Only show error for actual data failures
                    emit(UiState.Error(e.message ?: "Dashboard sync interrupted"))
                }
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
            val user = currentUser.value ?: getCurrentUserUseCase().filterNotNull().first()
            user.let {
                analyticsRepository.syncAnalytics(it.companyId)
                _refreshTrigger.emit(Unit)
            }
        }
    }
}
