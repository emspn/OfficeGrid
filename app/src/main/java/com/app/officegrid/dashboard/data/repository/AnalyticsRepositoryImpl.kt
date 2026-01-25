package com.app.officegrid.dashboard.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.dashboard.data.local.AnalyticsDao
import com.app.officegrid.dashboard.data.local.AnalyticsEntity
import com.app.officegrid.dashboard.data.remote.SupabaseAnalyticsDataSource
import com.app.officegrid.dashboard.domain.model.Analytics
import com.app.officegrid.dashboard.domain.repository.AnalyticsRepository
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : AnalyticsRepository {

    override fun getAnalytics(companyId: String): Flow<Analytics?> {
        return analyticsDao.getAnalytics(companyId).map { entity ->
            entity?.let {
                Analytics(
                    totalTasks = it.totalTasks,
                    completedTasks = it.completedTasks,
                    inProgressTasks = it.inProgressTasks,
                    pendingTasks = it.pendingTasks,
                    overdueTasks = it.overdueTasks,
                    tasksPerEmployee = Json.decodeFromString(it.tasksPerEmployeeJson),
                    completedTasksPerEmployee = Json.decodeFromString(it.completedTasksPerEmployeeJson)
                )
            }
        }
    }

    override suspend fun syncAnalytics(companyId: String): Result<Unit> {
        return try {
            val tasks = taskRepository.getTasks("").first() // Logic depends on RLS for admin
            
            val total = tasks.size
            val completed = tasks.count { it.status == TaskStatus.DONE }
            val inProgress = tasks.count { it.status == TaskStatus.IN_PROGRESS }
            val pending = tasks.count { it.status == TaskStatus.TODO }
            val overdue = tasks.count { it.dueDate < System.currentTimeMillis() && it.status != TaskStatus.DONE }
            
            val perEmployee = tasks.groupBy { it.assignedTo }.mapValues { it.value.size }
            val completedPerEmployee = tasks.filter { it.status == TaskStatus.DONE }
                .groupBy { it.assignedTo }.mapValues { it.value.size }

            val entity = AnalyticsEntity(
                companyId = companyId,
                totalTasks = total,
                completedTasks = completed,
                inProgressTasks = inProgress,
                pendingTasks = pending,
                overdueTasks = overdue,
                tasksPerEmployeeJson = Json.encodeToString(perEmployee),
                completedTasksPerEmployeeJson = Json.encodeToString(completedPerEmployee),
                updatedAt = System.currentTimeMillis()
            )
            
            analyticsDao.insertAnalytics(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}