package com.app.officegrid.tasks.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.tasks.data.local.TaskRemarkDao
import com.app.officegrid.tasks.data.local.TaskRemarkEntity
import com.app.officegrid.tasks.data.remote.SupabaseRemarkDataSource
import com.app.officegrid.tasks.data.remote.dto.TaskRemarkDto
import com.app.officegrid.tasks.data.remote.TaskRemarkRealtimeDataSource
import com.app.officegrid.tasks.domain.model.TaskRemark
import com.app.officegrid.tasks.domain.repository.TaskRemarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRemarkRepositoryImpl @Inject constructor(
    private val remarkDao: TaskRemarkDao,
    private val remoteDataSource: SupabaseRemarkDataSource,
    private val authRepository: AuthRepository,
    private val realtimeDataSource: TaskRemarkRealtimeDataSource
) : TaskRemarkRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var insertJob: Job? = null
    private var deleteJob: Job? = null

    init {
        startRealtimeSync()
    }

    private fun startRealtimeSync() {
        stopRealtimeSync()

        // Subscribe to new remarks
        insertJob = scope.launch {
            try {
                realtimeDataSource.subscribeToRemarkInserts().collect { remarkDto ->
                    Timber.d("REALTIME: New remark for task ${remarkDto.task_id}")
                    remarkDao.insertRemarks(listOf(remarkDto.toEntity()))
                }
            } catch (e: Exception) {
                Timber.e(e, "REALTIME: Insert subscription error")
            }
        }

        // Subscribe to deleted remarks
        deleteJob = scope.launch {
            try {
                realtimeDataSource.subscribeToRemarkDeletes().collect { remarkId ->
                    if (remarkId.isNotEmpty()) {
                        Timber.d("REALTIME: Deleted remark $remarkId")
                        remarkDao.deleteRemarkById(remarkId)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "REALTIME: Delete subscription error")
            }
        }
    }

    private fun stopRealtimeSync() {
        insertJob?.cancel()
        deleteJob?.cancel()
    }

    override suspend fun addTaskRemark(taskId: String, message: String): Result<Unit> {
        return try {
            val user = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))

            val remarkDto = TaskRemarkDto(
                task_id = taskId,
                user_id = user.id,
                user_name = user.fullName,
                content = message
            )

            remoteDataSource.insertRemark(remarkDto)
            // No need to manually sync, Realtime will catch it
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
            Timber.d("SYNC: Fetching remarks for task $taskId")
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

    private fun TaskRemarkDto.toEntity(): TaskRemarkEntity {
        val timestamp = try {
            if (created_at != null) {
                OffsetDateTime.parse(created_at).toInstant().toEpochMilli()
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        return TaskRemarkEntity(
            id = id ?: java.util.UUID.randomUUID().toString(),
            taskId = task_id,
            message = content,
            createdBy = user_name,
            createdAt = timestamp
        )
    }
}
