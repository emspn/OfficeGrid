package com.app.officegrid.organization.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.auth.domain.usecase.GetCurrentUserUseCase
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.team.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrganizationViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    val organizationData: StateFlow<UiState<OrganizationData>> = getCurrentUserUseCase()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(UiState.Error("User not found"))
            } else {
                employeeRepository.getEmployees(user.companyId).map { employees ->
                    // Calculate real metrics
                    val employeeCount = employees.size
                    val maxEmployees = 50 // Plan limit

                    // Use the REAL company name from signup
                    val companyName = user.companyName ?: "Unknown Company"

                    UiState.Success(
                        OrganizationData(
                            companyName = companyName,
                            companyId = user.companyId,
                            industry = "Technology", // TODO: Add industry field to User model
                            currentEmployees = employeeCount,
                            maxEmployees = maxEmployees,
                            storageUsed = "0 MB",
                            storageLimit = "10 GB",
                            planName = "Pro Plan"
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

data class OrganizationData(
    val companyName: String,
    val companyId: String,
    val industry: String,
    val currentEmployees: Int,
    val maxEmployees: Int,
    val storageUsed: String,
    val storageLimit: String,
    val planName: String
)
