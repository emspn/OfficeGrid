package com.app.officegrid.tasks.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.common.domain.model.AuditEventType
import com.app.officegrid.core.common.domain.repository.AuditLogRepository
import com.app.officegrid.core.notification.NotificationHelper
import com.app.officegrid.tasks.data.local.TaskDao
import com.app.officegrid.tasks.data.mapper.toDomain
import com.app.officegrid.tasks.data.mapper.toEntity
import com.app.officegrid.tasks.data.mapper.toDto
import com.app.officegrid.tasks.data.remote.SupabaseTaskDataSource
import com.app.officegrid.tasks.data.remote.TaskRealtimeEvent
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import com.app.officegrid.tasks.domain.repository.TaskRemarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val supabaseDataSource: SupabaseTaskDataSource,
    private val sessionManager: SessionManager,
    private val auditLogRepository: AuditLogRepository,
    private val notificationHelper: NotificationHelper,
    private val authRepository: AuthRepository,
    private val remarkRepository: TaskRemarkRepository
) : TaskRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val realtimeJobs = mutableMapOf<String, Job>()

    init {
        // Automatically manage realtime for the active node
        scope.launch {
            sessionManager.sessionState.collectLatest { state ->
                val activeId = state.activeCompanyId
                if (state.isLoggedIn && !activeId.isNullOrBlank()) {
                    startWorkspaceSync(activeId)
                } else {
                    stopAllSync()
                }
            }
        }
    }

    private fun startWorkspaceSync(companyId: String) {
        if (realtimeJobs.containsKey(companyId)) return
        
        realtimeJobs[companyId] = scope.launch {
            Timber.d("REALTIME: Initializing mission registry sync for node $companyId")
            syncTasks(companyId)
            
            supabaseDataSource.observeTasks(companyId).collect { event ->
                when (event) {
                    is TaskRealtimeEvent.Inserted -> {
                        taskDao.insertTasks(listOf(event.task.toEntity()))
                    }
                    is TaskRealtimeEvent.Updated -> {
                        taskDao.insertTasks(listOf(event.task.toEntity()))
                    }
                    is TaskRealtimeEvent.Deleted -> {
                        taskDao.deleteTask(event.taskId)
                    }
                }
            }
        }
    }

    private fun stopAllSync() {
        realtimeJobs.values.forEach { it.cancel() }
        realtimeJobs.clear()
    }

    override fun observeUserTasks(userId: String, companyId: String): Flow<List<Task>> {
        return taskDao.getTasksByCompany(companyId).map { entities ->
            entities.filter { it.assignedTo == userId }.map { it.toDomain() }
        }
    }

    override fun observeCompanyTasks(companyId: String): Flow<List<Task>> {
        return taskDao.getTasksByCompany(companyId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTaskById(taskId: String): Flow<Task?> {
        return taskDao.getTaskByIdFlow(taskId).map { it?.toDomain() }
    }

    override fun getTasks(userId: String): Flow<List<Task>> {
        val companyId = sessionManager.sessionState.value.activeCompanyId ?: ""
        return observeUserTasks(userId, companyId)
    }

    override fun getAllTasks(): Flow<List<Task>> {
        val companyId = sessionManager.sessionState.value.activeCompanyId ?: ""
        return observeCompanyTasks(companyId)
    }

    override suspend fun getTaskById(taskId: String): Result<Task> {
        return try {
            val local = taskDao.getTaskById(taskId)
            if (local != null) return Result.success(local.toDomain())
            
            val remote = supabaseDataSource.getTaskById(taskId)
            if (remote != null) {
                taskDao.insertTasks(listOf(remote.toEntity()))
                Result.success(remote.toDomain())
            } else {
                Result.failure(Exception("Mission not found in registry"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTask(task: Task): Result<Unit> {
        return try {
            val companyId = sessionManager.sessionState.value.activeCompanyId ?: return Result.failure(Exception("No active hub"))
            val user = authRepository.getCurrentUser().first() ?: return Result.failure(Exception("Not authorized"))
            
            supabaseDataSource.createTask(task.toDto(companyId))
            notificationHelper.notifyTaskAssigned(task, user.fullName)
            
            auditLogRepository.createAuditLog(
                type = AuditEventType.CREATE,
                title = "Mission Initialized",
                description = "Registry update: ${task.title}"
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            val companyId = sessionManager.sessionState.value.activeCompanyId ?: return Result.failure(Exception("No active hub"))
            supabaseDataSource.updateTask(task.toDto(companyId))
            notificationHelper.notifyTaskUpdated(task)
            
            auditLogRepository.createAuditLog(
                type = AuditEventType.UPDATE,
                title = "Mission Parameters Modified",
                description = "Reconfiguration of mission: ${task.title}"
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            supabaseDataSource.deleteTask(taskId)
            taskDao.deleteTask(taskId)
            
            auditLogRepository.createAuditLog(
                type = AuditEventType.DELETE,
                title = "Mission Terminated",
                description = "Registry entry $taskId removed"
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return try {
            supabaseDataSource.updateTaskStatus(taskId, status.name)
            taskDao.updateTaskStatus(taskId, status)
            
            val taskResult = getTaskById(taskId)
            taskResult.onSuccess { task ->
                val statusText = status.name.replace("_", " ")
                remarkRepository.addTaskRemark(taskId, "MISSION_STATUS_SYNC: $statusText")
                notificationHelper.notifyStatusChange(task, status)
            }
            
            auditLogRepository.createAuditLog(
                type = AuditEventType.STATUS_CHANGE,
                title = "Status Recalibrated",
                description = "Mission $taskId shifted to ${status.name}"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTasks(companyId: String): Result<Unit> {
        return try {
            val remoteTasks = supabaseDataSource.getTasks(companyId)
            if (remoteTasks.isEmpty()) {
                taskDao.deleteTasksByCompany(companyId)
            } else {
                val entities = remoteTasks.map { it.toEntity() }
                taskDao.insertTasks(entities)
                taskDao.deleteTasksByCompanyExceptIds(companyId, entities.map { it.id })
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearLocalData(): Result<Unit> {
        return try {
            taskDao.deleteAllTasks()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
