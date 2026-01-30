package com.app.officegrid.tasks.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.tasks.data.local.TaskDao
import com.app.officegrid.tasks.data.mapper.toDomain
import com.app.officegrid.tasks.data.mapper.toEntity
import com.app.officegrid.tasks.data.mapper.toDto
import com.app.officegrid.tasks.data.remote.TaskRemoteDataSource
import com.app.officegrid.tasks.data.remote.TaskRealtimeDataSource
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val remoteDataSource: TaskRemoteDataSource,
    private val authRepository: AuthRepository,
    private val realtimeDataSource: TaskRealtimeDataSource
) : TaskRepository {

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

        if (user == null) {
            android.util.Log.w("TaskRepository", "User not available for realtime sync")
            return
        }

        android.util.Log.d("TaskRepository", "Starting realtime sync for company: ${user.companyId}")

        // Subscribe to INSERT events
        insertJob = scope.launch {
            try {
                realtimeDataSource.subscribeToTaskInserts().collect { task ->
                    if (task.company_id == user.companyId) {
                        android.util.Log.d("TaskRepository", "Realtime INSERT: ${task.title}")
                        taskDao.insertTasks(listOf(task.toEntity()))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskRepository", "Realtime INSERT error: ${e.message}", e)
            }
        }

        // Subscribe to UPDATE events
        updateJob = scope.launch {
            try {
                realtimeDataSource.subscribeToTaskUpdates().collect { task ->
                    if (task.company_id == user.companyId) {
                        android.util.Log.d("TaskRepository", "Realtime UPDATE: ${task.title}")
                        taskDao.insertTasks(listOf(task.toEntity()))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskRepository", "Realtime UPDATE error: ${e.message}", e)
            }
        }

        // Subscribe to DELETE events
        deleteJob = scope.launch {
            try {
                realtimeDataSource.subscribeToTaskDeletes().collect { taskId ->
                    if (taskId.isNotEmpty()) {
                        android.util.Log.d("TaskRepository", "Realtime DELETE: $taskId")
                        taskDao.deleteTask(taskId)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskRepository", "Realtime DELETE error: ${e.message}", e)
            }
        }
    }

    private fun stopRealtimeSync() {
        insertJob?.cancel()
        updateJob?.cancel()
        deleteJob?.cancel()
    }

    override fun getTasks(userId: String): Flow<List<Task>> {
        return taskDao.getTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(taskId: String): Result<Task> {
        return try {
            val task = taskDao.getTaskById(taskId)?.toDomain()
            if (task != null) {
                Result.success(task)
            } else {
                val remoteTask = remoteDataSource.getTaskById(taskId)
                if (remoteTask != null) {
                    val domainTask = remoteTask.toDomain()
                    taskDao.insertTasks(listOf(remoteTask.toEntity()))
                    Result.success(domainTask)
                } else {
                    Result.failure(Exception("Task not found"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTask(task: Task): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))
            
            if (user.role != UserRole.ADMIN) {
                return Result.failure(SecurityException("Unauthorized: Only admins can create tasks"))
            }
            
            remoteDataSource.createTask(task.toDto(user.companyId))
            taskDao.insertTasks(listOf(task.toEntity()))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))
            
            if (user.role != UserRole.ADMIN) {
                return Result.failure(SecurityException("Unauthorized: Only admins can edit tasks"))
            }
            
            remoteDataSource.updateTask(task.toDto(user.companyId))
            taskDao.insertTasks(listOf(task.toEntity()))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))
            
            if (user.role != UserRole.ADMIN) {
                return Result.failure(SecurityException("Unauthorized: Only admins can delete tasks"))
            }
            
            remoteDataSource.deleteTask(taskId)
            taskDao.deleteTask(taskId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))
            
            val task = taskDao.getTaskById(taskId)?.toDomain() 
                ?: return Result.failure(Exception("Task not found"))

            val isAuthorized = user.role == UserRole.ADMIN || task.assignedTo == user.id
            
            if (!isAuthorized) {
                return Result.failure(SecurityException("Unauthorized: You cannot update this task"))
            }

            remoteDataSource.updateTaskStatus(taskId, status)
            taskDao.updateTaskStatus(taskId, status)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTasks(userId: String): Result<Unit> {
        return try {
            android.util.Log.d("TaskRepository", "syncTasks called for userId: $userId")
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))
            
            android.util.Log.d("TaskRepository", "User: ${user.email}, Role: ${user.role}, CompanyId: ${user.companyId}")
            val remoteTasks = remoteDataSource.getTasks(userId, user.role, user.companyId)
            android.util.Log.d("TaskRepository", "Fetched ${remoteTasks.size} tasks from Supabase")
            remoteTasks.forEach { task ->
                android.util.Log.d("TaskRepository", "Task: ${task.title}, assignedTo: ${task.assigned_to}")
            }

            val entities = remoteTasks.map { it.toEntity() }
            
            taskDao.insertTasks(entities)
            android.util.Log.d("TaskRepository", "Inserted ${entities.size} tasks into local DB")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("TaskRepository", "Error syncing tasks: ${e.message}", e)
            Result.failure(e)
        }
    }
}
