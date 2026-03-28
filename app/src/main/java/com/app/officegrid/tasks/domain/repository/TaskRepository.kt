package com.app.officegrid.tasks.domain.repository

import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    /**
     * Observe missions assigned to a specific user.
     */
    fun getTasks(userId: String): Flow<List<Task>>
    
    /**
     * Observe all missions in the active node (Admin use case).
     */
    fun getAllTasks(): Flow<List<Task>>
    
    /**
     * Reactive stream for a specific mission by ID.
     */
    fun observeTaskById(taskId: String): Flow<Task?>
    
    /**
     * Observe missions for a specific user within a specific company.
     */
    fun observeUserTasks(userId: String, companyId: String): Flow<List<Task>>
    
    /**
     * Observe all missions for a specific company node.
     */
    fun observeCompanyTasks(companyId: String): Flow<List<Task>>
    
    suspend fun getTaskById(taskId: String): Result<Task>
    suspend fun createTask(task: Task): Result<Unit>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit>
    
    /**
     * Force cloud sync for a specific node registry.
     */
    suspend fun syncTasks(companyId: String): Result<Unit>

    suspend fun clearLocalData(): Result<Unit>
}
