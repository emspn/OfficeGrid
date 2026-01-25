package com.app.officegrid.team.domain.repository

import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.model.EmployeeStatus
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    fun getEmployees(companyId: String): Flow<List<Employee>>
    suspend fun syncEmployees(companyId: String): Result<Unit>
    suspend fun updateEmployeeStatus(employeeId: String, status: EmployeeStatus): Result<Unit>
}