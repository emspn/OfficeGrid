package com.app.officegrid.team.domain.repository

import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.model.EmployeeStatus
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    fun getEmployees(companyId: String): Flow<List<Employee>>
    fun getEmployeesByUserId(userId: String): Flow<List<Employee>>
    suspend fun syncEmployees(companyId: String): Result<Unit>
    suspend fun syncEmployeesByUserId(userId: String): Result<Unit>
    suspend fun updateEmployeeStatus(employeeId: String, status: EmployeeStatus): Result<Unit>
    suspend fun deleteEmployee(employeeId: String): Result<Unit>
    suspend fun leaveWorkspace(userId: String, companyId: String): Result<Unit>
    suspend fun updateEmployeeRole(employeeId: String, newRole: String): Result<Unit>
    suspend fun joinWorkspace(userId: String, userName: String, userEmail: String, companyId: String): Result<Unit>
    suspend fun validateWorkspaceCode(code: String): Boolean
    suspend fun getOrganizationName(companyId: String): String?
}
