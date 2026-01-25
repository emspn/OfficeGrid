package com.app.officegrid.team.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.team.data.local.EmployeeDao
import com.app.officegrid.team.data.local.EmployeeEntity
import com.app.officegrid.team.data.remote.EmployeeDto
import com.app.officegrid.team.data.remote.SupabaseEmployeeDataSource
import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.model.EmployeeStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EmployeeRepositoryImpl @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val remoteDataSource: SupabaseEmployeeDataSource,
    private val authRepository: AuthRepository
) : EmployeeRepository {

    override fun getEmployees(companyId: String): Flow<List<Employee>> {
        return employeeDao.getEmployeesByCompany(companyId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncEmployees(companyId: String): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("Not authenticated"))
            
            if (user.role != UserRole.ADMIN) {
                return Result.failure(SecurityException("Unauthorized"))
            }

            val remoteEmployees = remoteDataSource.getEmployeesByCompany(companyId)
            val entities = remoteEmployees.map { it.toEntity() }
            
            employeeDao.deleteEmployeesByCompany(companyId)
            employeeDao.insertEmployees(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmployeeStatus(employeeId: String, status: EmployeeStatus): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("Not authenticated"))

            if (user.role != UserRole.ADMIN) {
                return Result.failure(SecurityException("Only organisations can approve employees"))
            }

            remoteDataSource.updateEmployeeStatus(employeeId, status.name)
            // Update local Room cache as well
            // We'll need a way to update status in Dao without refetching all
            // For now, simple implementation:
            syncEmployees(user.companyId) 
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun EmployeeEntity.toDomain() = Employee(id, name, email, role, companyId, status)
    
    private fun EmployeeDto.toEntity() = EmployeeEntity(
        id = id,
        name = name,
        email = email,
        role = role,
        companyId = company_id,
        status = try { EmployeeStatus.valueOf(status.uppercase()) } catch (e: Exception) { EmployeeStatus.PENDING }
    )
}