package com.app.officegrid.tasks.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.tasks.data.local.TaskDao
import com.app.officegrid.tasks.data.mapper.toDomain
import com.app.officegrid.tasks.data.mapper.toEntity
import com.app.officegrid.tasks.data.mapper.toDto
import com.app.officegrid.tasks.data.remote.TaskRemoteDataSource
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val remoteDataSource: TaskRemoteDataSource,
    private val authRepository: AuthRepository
) : TaskRepository {

    override fun getTasks(userId: String): Flow<List<Task>> {
        return taskDao.getTasks().map { entities ->
            if (entities.isEmpty()) {
                syncTasks(userId)
            }
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(taskId: String): Result<Task> {
        return try {
            val task = taskDao.getTaskById(taskId)?.toDomain()
            if (task != null) {
                Result.success(task)
            } else {
                Result.failure(Exception("Task not found"))
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
            taskDao.deleteTask(taskId) // Note: Need to add deleteTask to TaskDao
            
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
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))
            
            val remoteTasks = remoteDataSource.getTasks(userId, user.role)
            val entities = remoteTasks.map { it.toEntity() }
            
            taskDao.insertTasks(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}