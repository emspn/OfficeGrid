package com.app.officegrid.tasks.domain.repository

import com.app.officegrid.tasks.domain.model.TaskRemark
import kotlinx.coroutines.flow.Flow

interface TaskRemarkRepository {
    suspend fun addTaskRemark(taskId: String, message: String): Result<Unit>
    fun getTaskRemarks(taskId: String): Flow<List<TaskRemark>>
    suspend fun syncRemarks(taskId: String): Result<Unit>
}
