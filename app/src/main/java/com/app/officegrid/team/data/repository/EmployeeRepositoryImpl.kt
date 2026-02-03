package com.app.officegrid.team.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.core.common.domain.model.AuditEventType
import com.app.officegrid.core.common.domain.repository.AuditLogRepository
import com.app.officegrid.core.notification.NotificationHelper
import com.app.officegrid.team.data.local.EmployeeDao
import com.app.officegrid.team.data.local.EmployeeEntity
import com.app.officegrid.team.data.remote.EmployeeDto
import com.app.officegrid.team.data.remote.EmployeeRealtimeEvent
import com.app.officegrid.team.data.remote.SupabaseEmployeeDataSource
import com.app.officegrid.team.domain.model.Employee
import com.app.officegrid.team.domain.model.EmployeeStatus
import com.app.officegrid.team.domain.repository.EmployeeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepositoryImpl @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val remoteDataSource: SupabaseEmployeeDataSource,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val auditLogRepository: AuditLogRepository,
    private val notificationHelper: NotificationHelper
) : EmployeeRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var realtimeJob: Job? = null

    init {
        // 🔄 Multi-Workspace Aware Sync
        scope.launch {
            sessionManager.sessionState.collectLatest { state ->
                if (state.isLoggedIn && state.activeCompanyId != null) {
                    startWorkspaceSync(state.activeCompanyId)
                } else {
                    stopWorkspaceSync()
                }
            }
        }
    }

    private fun startWorkspaceSync(companyId: String) {
        realtimeJob?.cancel()
        realtimeJob = scope.launch {
            // Initial Sync
            syncEmployees(companyId)
            
            // Realtime Listen
            remoteDataSource.observeEmployees(companyId).collect { event ->
                when (event) {
                    is EmployeeRealtimeEvent.Inserted -> {
                        employeeDao.insertEmployees(listOf(event.employee.toEntity()))
                    }
                    is EmployeeRealtimeEvent.Updated -> {
                        employeeDao.insertEmployees(listOf(event.employee.toEntity()))
                    }
                    is EmployeeRealtimeEvent.Deleted -> {
                        employeeDao.deleteEmployeeFromWorkspace(event.employeeId, companyId)
                    }
                }
            }
        }
    }

    private fun stopWorkspaceSync() {
        realtimeJob?.cancel()
    }

    override fun getEmployees(companyId: String): Flow<List<Employee>> {
        return employeeDao.getEmployeesByCompany(companyId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEmployeesByUserId(userId: String): Flow<List<Employee>> {
        return employeeDao.getEmployeesByUserId(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncEmployees(companyId: String): Result<Unit> {
        return try {
            val remoteEmployees = remoteDataSource.getEmployeesByCompany(companyId)
            employeeDao.deleteEmployeesByCompany(companyId)
            employeeDao.insertEmployees(remoteEmployees.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncEmployeesByUserId(userId: String): Result<Unit> {
        return try {
            val remoteEmployees = remoteDataSource.getEmployeesByUserId(userId)
            employeeDao.deleteEmployeesByUserId(userId)
            employeeDao.insertEmployees(remoteEmployees.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmployeeStatus(employeeId: String, status: EmployeeStatus): Result<Unit> {
        return try {
            val companyId = sessionManager.sessionState.value.activeCompanyId ?: return Result.failure(Exception("No active workspace"))
            
            remoteDataSource.updateEmployeeStatus(employeeId, companyId, status.name)
            employeeDao.updateEmployeeStatus(employeeId, status)
            
            auditLogRepository.createAuditLog(
                type = AuditEventType.UPDATE,
                title = "EMPLOYEE_STATUS_CHANGE",
                description = "Employee status updated to ${status.name}"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEmployee(employeeId: String): Result<Unit> {
        return try {
            val companyId = sessionManager.sessionState.value.activeCompanyId ?: return Result.failure(Exception("No active workspace"))
            
            remoteDataSource.deleteEmployee(employeeId, companyId)
            employeeDao.deleteEmployeeFromWorkspace(employeeId, companyId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveWorkspace(userId: String, companyId: String): Result<Unit> {
        return try {
            remoteDataSource.deleteEmployee(userId, companyId)
            employeeDao.deleteEmployeeFromWorkspace(userId, companyId)
            
            // If leaving active workspace, clear it from session
            if (sessionManager.sessionState.value.activeCompanyId == companyId) {
                sessionManager.logout() // Or just clear workspace
            }
            
            auditLogRepository.createAuditLog(
                type = AuditEventType.DELETE,
                title = "WORKSPACE_LEAVE",
                description = "User left workspace $companyId"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmployeeRole(employeeId: String, newRole: String): Result<Unit> {
        return try {
            val companyId = sessionManager.sessionState.value.activeCompanyId ?: return Result.failure(Exception("No active workspace"))
            
            remoteDataSource.updateEmployeeRole(employeeId, companyId, newRole)
            employeeDao.updateEmployeeRole(employeeId, newRole)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinWorkspace(userId: String, userName: String, userEmail: String, companyId: String): Result<Unit> {
        return try {
            remoteDataSource.joinWorkspace(userId, userName, userEmail, companyId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateWorkspaceCode(code: String): Boolean {
        return remoteDataSource.validateWorkspaceCode(code)
    }

    override suspend fun getOrganizationName(companyId: String): String? {
        return remoteDataSource.getOrganizationName(companyId)
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
