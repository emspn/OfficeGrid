package com.app.officegrid.team.domain.usecase

import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEmployeesUseCase @Inject constructor(
    private val repository: EmployeeRepository
) {
    operator fun invoke(companyId: String): Flow<List<Employee>> {
        return repository.getEmployees(companyId)
    }
}