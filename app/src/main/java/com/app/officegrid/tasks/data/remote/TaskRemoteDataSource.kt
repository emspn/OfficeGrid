package com.app.officegrid.tasks.data.remote

import com.app.officegrid.core.common.UserRole
import com.app.officegrid.tasks.data.remote.dto.TaskDto
import com.app.officegrid.tasks.domain.model.TaskStatus

interface TaskRemoteDataSource {
    suspend fun getTasks(userId: String, role: UserRole, companyId: String): List<TaskDto>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus)
    suspend fun createTask(task: TaskDto)
    suspend fun updateTask(task: TaskDto)
    suspend fun deleteTask(taskId: String)
    suspend fun getTaskById(taskId: String): TaskDto?
}

