package com.app.officegrid.tasks.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.tasks.data.local.TaskRemarkDao
import com.app.officegrid.tasks.data.local.TaskRemarkEntity
import com.app.officegrid.tasks.data.remote.SupabaseRemarkDataSource
import com.app.officegrid.tasks.data.remote.TaskRemarkDto
import com.app.officegrid.tasks.domain.model.TaskRemark
import com.app.officegrid.tasks.domain.repository.TaskRemarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRemarkRepositoryImpl @Inject constructor(
    private val remarkDao: TaskRemarkDao,
    private val remoteDataSource: SupabaseRemarkDataSource,
    private val authRepository: AuthRepository
) : TaskRemarkRepository {

    override suspend fun addTaskRemark(taskId: String, message: String): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))

            val remarkDto = TaskRemarkDto(
                task_id = taskId,
                message = message,
                created_by = user.id
            )

            remoteDataSource.insertRemark(remarkDto)
            // Sync immediately after success
            syncRemarks(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTaskRemarks(taskId: String): Flow<List<TaskRemark>> {
        return remarkDao.getRemarksForTask(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncRemarks(taskId: String): Result<Unit> {
        return try {
            val remoteRemarks = remoteDataSource.getRemarksForTask(taskId)
            val entities = remoteRemarks.map { it.toEntity() }
            remarkDao.deleteRemarksForTask(taskId)
            remarkDao.insertRemarks(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun TaskRemarkEntity.toDomain() = TaskRemark(
        id = id,
        taskId = taskId,
        message = message,
        createdBy = createdBy,
        createdAt = createdAt
    )

    private fun TaskRemarkDto.toEntity() = TaskRemarkEntity(
        id = id ?: "",
        taskId = task_id,
        message = message,
        createdBy = created_by,
        createdAt = 0L // Future: Handle timestamp parsing
    )
}