package com.app.officegrid.team.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.notification.NotificationHelper
import com.app.officegrid.tasks.data.local.TaskDao
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepositoryImpl @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val taskDao: TaskDao,
    private val remoteDataSource: SupabaseEmployeeDataSource,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val notificationHelper: NotificationHelper
) : EmployeeRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var workspaceRealtimeJob: Job? = null
    private var globalMembershipJob: Job? = null

    init {
        observeSessionChanges()
    }

    private fun observeSessionChanges() {
        scope.launch {
            sessionManager.sessionState.collectLatest { state ->
                if (state.isLoggedIn) {
                    delay(1000)
                    
                    val activeCompanyId = state.activeCompanyId
                    if (!activeCompanyId.isNullOrBlank()) {
                        startWorkspaceSync(activeCompanyId)
                    } else {
                        stopWorkspaceSync()
                    }
                    
                    val userId = state.userId
                    if (!userId.isNullOrBlank()) {
                        startGlobalMembershipSync(userId)
                    }
                } else {
                    stopWorkspaceSync()
                    stopGlobalMembershipSync()
                }
            }
        }
    }

    private fun startWorkspaceSync(companyId: String) {
        workspaceRealtimeJob?.cancel()
        workspaceRealtimeJob = scope.launch {
            syncEmployees(companyId)
            remoteDataSource.observeEmployees(companyId).collect { event ->
                handleRealtimeEvent(event, companyId)
            }
        }
    }

    private fun startGlobalMembershipSync(userId: String) {
        globalMembershipJob?.cancel()
        globalMembershipJob = scope.launch {
            syncEmployeesByUserId(userId)
            remoteDataSource.observeUserMemberships(userId).collect { event ->
                handleRealtimeEvent(event, "")
            }
        }
    }

    private fun handleRealtimeEvent(event: EmployeeRealtimeEvent, currentContextId: String) {
        scope.launch {
            when (event) {
                is EmployeeRealtimeEvent.Inserted, is EmployeeRealtimeEvent.Updated -> {
                    val dto = if (event is EmployeeRealtimeEvent.Inserted) event.employee else (event as EmployeeRealtimeEvent.Updated).employee
                    val orgName = remoteDataSource.getOrganizationName(dto.company_id)
                    employeeDao.insertEmployees(listOf(dto.toEntity(orgName)))
                }
                is EmployeeRealtimeEvent.Deleted -> {
                    val employeeId = event.employeeId
                    val companyId = if (event.companyId.isNotBlank()) event.companyId else currentContextId
                    
                    if (employeeId.isNotBlank() && companyId.isNotBlank()) {
                        Timber.d("SYNC: Removing employee $employeeId from workspace $companyId locally via Realtime")
                        employeeDao.deleteEmployeeFromWorkspace(employeeId, companyId)
                        
                        // If it's the current user, clean up their session
                        if (employeeId == sessionManager.sessionState.value.userId) {
                            taskDao.deleteTasksByCompany(companyId)
                            if (sessionManager.sessionState.value.activeCompanyId == companyId) {
                                sessionManager.switchWorkspace("", false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopWorkspaceSync() { workspaceRealtimeJob?.cancel() }
    private fun stopGlobalMembershipSync() { globalMembershipJob?.cancel() }

    override fun getEmployees(companyId: String): Flow<List<Employee>> = 
        employeeDao.getEmployeesByCompany(companyId).map { it.map { e -> e.toDomain() } }

    override fun getEmployeesByUserId(userId: String): Flow<List<Employee>> = 
        employeeDao.getEmployeesByUserId(userId).map { it.map { e -> e.toDomain() } }

    override fun getPendingRequestsCount(companyId: String): Flow<Int> {
        return employeeDao.getEmployeesByCompany(companyId).map { entities ->
            entities.count { it.status == EmployeeStatus.PENDING }
        }
    }

    override suspend fun syncEmployees(companyId: String): Result<Unit> = try {
        val remote = remoteDataSource.getEmployeesByCompany(companyId)
        val orgName = remoteDataSource.getOrganizationName(companyId)
        val entities = remote.map { it.toEntity(orgName) }
        employeeDao.syncCompanyEmployees(companyId, entities)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun syncEmployeesByUserId(userId: String): Result<Unit> = try {
        val remote = remoteDataSource.getEmployeesByUserId(userId)
        val entities = remote.map { dto ->
            val orgName = remoteDataSource.getOrganizationName(dto.company_id)
            dto.toEntity(orgName)
        }
        employeeDao.syncUserWorkspaces(userId, entities)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateEmployeeStatus(employeeId: String, status: EmployeeStatus): Result<Unit> = try {
        val companyId = sessionManager.sessionState.value.activeCompanyId ?: throw Exception("No active workspace")
        remoteDataSource.updateEmployeeStatus(employeeId, companyId, status.name)
        employeeDao.updateEmployeeStatusWithCompany(employeeId, companyId, status)
        
        if (status == EmployeeStatus.APPROVED) {
            val orgName = remoteDataSource.getOrganizationName(companyId) ?: "the organization"
            notificationHelper.notifyJoinApproved(employeeId, orgName)
        }
        
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteEmployee(employeeId: String): Result<Unit> = try {
        val companyId = sessionManager.sessionState.value.activeCompanyId ?: throw Exception("No active workspace")
        
        // 1. Remote Delete
        remoteDataSource.deleteEmployee(employeeId, companyId)
        
        // 2. Local Cleanup
        employeeDao.deleteEmployeeFromWorkspace(employeeId, companyId)
        
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun leaveWorkspace(userId: String, companyId: String): Result<Unit> = try {
        // 1. Remote Delete
        remoteDataSource.deleteEmployee(userId, companyId)
        
        // 2. Local Cleanup
        employeeDao.deleteEmployeeFromWorkspace(userId, companyId)
        taskDao.deleteTasksByCompany(companyId)
        
        if (sessionManager.sessionState.value.activeCompanyId == companyId) {
            sessionManager.switchWorkspace("", false)
        }

        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateEmployeeRole(employeeId: String, newRole: String): Result<Unit> = try {
        val companyId = sessionManager.sessionState.value.activeCompanyId ?: throw Exception("No active workspace")
        remoteDataSource.updateEmployeeRole(employeeId, companyId, newRole)
        syncEmployees(companyId)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun joinWorkspace(userId: String, userName: String, userEmail: String, companyId: String): Result<Unit> = try {
        val existing = employeeDao.getEmployeeByUserAndCompany(userId, companyId)
        if (existing != null) {
            throw Exception("You have already joined or requested this workspace.")
        }
        
        val orgName = remoteDataSource.getOrganizationName(companyId) ?: throw Exception("Workspace ID not found.")

        remoteDataSource.joinWorkspace(userId, userName, userEmail, companyId)
        syncEmployeesByUserId(userId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun validateWorkspaceCode(code: String): Result<Boolean> = try {
        Result.success(remoteDataSource.validateWorkspaceCode(code))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getOrganizationName(companyId: String): String? = remoteDataSource.getOrganizationName(companyId)
    override suspend fun getWorkspaceAdminId(companyId: String): String? = remoteDataSource.getWorkspaceAdminId(companyId)

    private fun EmployeeEntity.toDomain() = Employee(id, name, email, role, companyId, status, companyName)
    
    private fun EmployeeDto.toEntity(companyName: String? = null) = EmployeeEntity(
        id = id, 
        name = name, 
        email = email, 
        role = role, 
        companyId = company_id,
        status = if (is_approved || status.uppercase() == "APPROVED") EmployeeStatus.APPROVED else EmployeeStatus.PENDING,
        companyName = companyName
    )
}
