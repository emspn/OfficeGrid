package com.app.officegrid.team.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.team.data.local.EmployeeDao
import com.app.officegrid.team.data.local.EmployeeEntity
import com.app.officegrid.team.data.remote.EmployeeDto
import com.app.officegrid.team.data.remote.EmployeeRealtimeDataSource
import com.app.officegrid.team.data.remote.SupabaseEmployeeDataSource
import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.model.EmployeeStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class EmployeeRepositoryImpl @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val remoteDataSource: SupabaseEmployeeDataSource,
    private val authRepository: AuthRepository,
    private val realtimeDataSource: EmployeeRealtimeDataSource
) : EmployeeRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var insertJob: Job? = null
    private var updateJob: Job? = null
    private var deleteJob: Job? = null

    init {
        startRealtimeSync()
    }

    private fun startRealtimeSync() {
        stopRealtimeSync()

        val user = try {
            runBlocking { authRepository.getCurrentUser().first() }
        } catch (e: Exception) {
            null
        }

        if (user == null || user.role != UserRole.ADMIN) {
            android.util.Log.w("EmployeeRepo", "User not admin or not available for realtime sync")
            return
        }

        android.util.Log.d("EmployeeRepo", "Starting realtime sync for company: ${user.companyId}")

        // Subscribe to INSERT events (new employee requests)
        insertJob = scope.launch {
            try {
                realtimeDataSource.subscribeToEmployeeInserts().collect { employee ->
                    if (employee.company_id == user.companyId) {
                        android.util.Log.d("EmployeeRepo", "Realtime INSERT: ${employee.name}")
                        employeeDao.insertEmployees(listOf(employee.toEntity()))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("EmployeeRepo", "Realtime INSERT error: ${e.message}", e)
            }
        }

        // Subscribe to UPDATE events (approval status changes)
        updateJob = scope.launch {
            try {
                realtimeDataSource.subscribeToEmployeeUpdates().collect { employee ->
                    if (employee.company_id == user.companyId) {
                        android.util.Log.d("EmployeeRepo", "Realtime UPDATE: ${employee.name}, approved: ${employee.is_approved}")
                        employeeDao.insertEmployees(listOf(employee.toEntity()))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("EmployeeRepo", "Realtime UPDATE error: ${e.message}", e)
            }
        }

        // Subscribe to DELETE events (rejected requests)
        deleteJob = scope.launch {
            try {
                realtimeDataSource.subscribeToEmployeeDeletes().collect { employeeId ->
                    if (employeeId.isNotEmpty()) {
                        android.util.Log.d("EmployeeRepo", "Realtime DELETE: $employeeId")
                        employeeDao.deleteEmployee(employeeId)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("EmployeeRepo", "Realtime DELETE error: ${e.message}", e)
            }
        }
    }

    private fun stopRealtimeSync() {
        insertJob?.cancel()
        updateJob?.cancel()
        deleteJob?.cancel()
    }

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

            android.util.Log.d("EmployeeRepo", "Syncing employees for company: $companyId")
            val remoteEmployees = remoteDataSource.getEmployeesByCompany(companyId)
            android.util.Log.d("EmployeeRepo", "Fetched ${remoteEmployees.size} employees from Supabase")
            remoteEmployees.forEach {
                android.util.Log.d("EmployeeRepo", "Employee: ${it.name}, email: ${it.email}, approved: ${it.is_approved}")
            }

            val entities = remoteEmployees.map { it.toEntity() }
            
            employeeDao.deleteEmployeesByCompany(companyId)
            employeeDao.insertEmployees(entities)
            android.util.Log.d("EmployeeRepo", "Inserted ${entities.size} employees into local DB")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("EmployeeRepo", "Error syncing employees: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateEmployeeStatus(employeeId: String, status: EmployeeStatus): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("Not authenticated"))

            if (user.role != UserRole.ADMIN) {
                return Result.failure(SecurityException("Only admins can manage operatives"))
            }

            // OPTIMISTIC UPDATE: Update local cache first for instant UI response
            employeeDao.updateEmployeeStatus(employeeId, status)

            // Perform remote update
            remoteDataSource.updateEmployeeStatus(employeeId, status.name)
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Re-sync on failure to ensure data consistency if the network call failed
            val user = authRepository.getCurrentUser().first()
            user?.let { syncEmployees(it.companyId) }
            Result.failure(e)
        }
    }

    override suspend fun deleteEmployee(employeeId: String): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("Not authenticated"))

            if (user.role != UserRole.ADMIN) {
                return Result.failure(SecurityException("Only admins can manage operatives"))
            }

            // OPTIMISTIC UPDATE: Remove from local cache immediately
            employeeDao.deleteEmployee(employeeId)

            // Perform remote deletion
            remoteDataSource.deleteEmployee(employeeId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            val user = authRepository.getCurrentUser().first()
            user?.let { syncEmployees(it.companyId) }
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
        status = if (is_approved) EmployeeStatus.APPROVED else EmployeeStatus.PENDING
    )
}
