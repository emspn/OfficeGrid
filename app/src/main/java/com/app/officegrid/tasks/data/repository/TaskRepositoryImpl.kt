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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    private var realtimeJob: Job? = null

    init {
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

    private fun stopWorkspaceSync() {
        realtimeJob?.cancel()
    }

    override fun getTasks(userId: String): Flow<List<Task>> {
        val companyId = sessionManager.sessionState.value.activeCompanyId ?: ""
        return taskDao.getTasksByCompany(companyId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllTasks(): Flow<List<Task>> {
        val companyId = sessionManager.sessionState.value.activeCompanyId ?: ""
        return taskDao.getTasksByCompany(companyId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTaskById(taskId: String): Flow<Task?> {
        return taskDao.getTaskByIdFlow(taskId).map { it?.toDomain() }
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
                Result.failure(Exception("Task not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTask(task: Task): Result<Unit> {
        return try {
            val companyId = sessionManager.sessionState.value.activeCompanyId ?: return Result.failure(Exception("No active workspace"))
            val user = authRepository.getCurrentUser().first() ?: return Result.failure(Exception("Not authenticated"))
            
            supabaseDataSource.createTask(task.toDto(companyId))
            
            // 🚀 COMMUNICATION: Alert Employee
            notificationHelper.notifyTaskAssigned(task, user.fullName)
            
            auditLogRepository.createAuditLog(
                type = AuditEventType.CREATE,
                title = "Task Created",
                description = "Node Admin initialized task: ${task.title}"
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            val companyId = sessionManager.sessionState.value.activeCompanyId ?: return Result.failure(Exception("No active workspace"))
            supabaseDataSource.updateTask(task.toDto(companyId))
            
            // Trigger Notification
            notificationHelper.notifyTaskUpdated(task)
            
            auditLogRepository.createAuditLog(
                type = AuditEventType.UPDATE,
                title = "Task Updated",
                description = "Task '${task.title}' was modified by Admin"
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
                title = "Task Deleted",
                description = "Task unit $taskId removed from operational registry"
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
            
            // 🚀 COMMUNICATION: Auto-log history & Notify Admin
            val taskResult = getTaskById(taskId)
            taskResult.onSuccess { task ->
                val statusText = status.name.replace("_", " ")
                remarkRepository.addTaskRemark(taskId, "System Log: Status transitioned to $statusText")
                notificationHelper.notifyStatusChange(task, status)
            }
            
            auditLogRepository.createAuditLog(
                type = AuditEventType.STATUS_CHANGE,
                title = "Status Sync",
                description = "Task $taskId set to ${status.name}"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTasks(userId: String): Result<Unit> {
        return try {
            val companyId = sessionManager.sessionState.value.activeCompanyId ?: userId
            val remoteTasks = supabaseDataSource.getTasks(companyId)
            taskDao.deleteTasksByCompany(companyId)
            taskDao.insertTasks(remoteTasks.map { it.toEntity() })
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
