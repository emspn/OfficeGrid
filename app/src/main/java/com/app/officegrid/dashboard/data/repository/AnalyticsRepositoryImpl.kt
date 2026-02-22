package com.app.officegrid.dashboard.data.repository

import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.dashboard.data.local.AnalyticsDao
import com.app.officegrid.dashboard.data.local.AnalyticsEntity
import com.app.officegrid.dashboard.data.remote.SupabaseAnalyticsDataSource
import com.app.officegrid.dashboard.domain.model.Analytics
import com.app.officegrid.dashboard.domain.repository.AnalyticsRepository
import com.app.officegrid.dashboard.presentation.PerformanceItem
import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val supabaseDataSource: SupabaseAnalyticsDataSource,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : AnalyticsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getAnalytics(companyId: String): Flow<Analytics?> {
        return analyticsDao.getAnalytics(companyId).map { entity ->
            entity?.let {
                Analytics(
                    totalTasks = it.totalTasks,
                    completedTasks = it.completedTasks,
                    inProgressTasks = it.inProgressTasks,
                    pendingTasks = it.pendingTasks,
                    totalEmployees = it.totalEmployees,
                    pendingApprovals = it.pendingApprovals,
                    overdueTasks = it.overdueTasks,
                    tasksPerEmployee = try { json.decodeFromString(it.tasksPerEmployeeJson) } catch (e: Exception) { emptyMap() },
                    completedTasksPerEmployee = try { json.decodeFromString(it.completedTasksPerEmployeeJson) } catch (e: Exception) { emptyMap() }
                )
            }
        }
    }

    override suspend fun syncAnalytics(companyId: String): Result<Unit> {
        return try {
            val remoteDto = supabaseDataSource.getAnalytics(companyId)
            val tasks = taskRepository.getAllTasks().first()
            
            val total = remoteDto?.total_tasks ?: tasks.size
            val completed = remoteDto?.completed_tasks ?: tasks.count { it.status == TaskStatus.DONE }
            val inProgress = remoteDto?.in_progress_tasks ?: tasks.count { it.status == TaskStatus.IN_PROGRESS }
            val pending = remoteDto?.pending_tasks ?: tasks.count { it.status == TaskStatus.TODO }
            val overdue = tasks.count { it.dueDate < System.currentTimeMillis() && it.status != TaskStatus.DONE }
            
            val perEmployee = tasks.filter { it.assignedTo.isNotEmpty() }
                .groupBy { it.assignedTo }
                .mapValues { it.value.size }
            
            val completedPerEmployee = tasks.filter { it.status == TaskStatus.DONE && it.assignedTo.isNotEmpty() }
                .groupBy { it.assignedTo }
                .mapValues { it.value.size }

            val entity = AnalyticsEntity(
                companyId = companyId,
                totalTasks = total,
                completedTasks = completed,
                inProgressTasks = inProgress,
                pendingTasks = pending,
                totalEmployees = remoteDto?.total_employees ?: 0,
                pendingApprovals = remoteDto?.pending_approvals ?: 0,
                overdueTasks = overdue,
                tasksPerEmployeeJson = json.encodeToString(perEmployee),
                completedTasksPerEmployeeJson = json.encodeToString(completedPerEmployee),
                updatedAt = System.currentTimeMillis()
            )
            
            analyticsDao.insertAnalytics(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTeamPerformance(companyId: String): Result<List<PerformanceItem>> {
        return try {
            val performance = supabaseDataSource.getTeamPerformance(companyId).map { dto ->
                PerformanceItem(
                    employeeId = dto.employee_id,
                    employeeName = dto.employee_name,
                    tasksAssigned = dto.tasks_assigned,
                    tasksCompleted = dto.tasks_completed,
                    completionRate = if (dto.tasks_assigned > 0) dto.tasks_completed.toFloat() / dto.tasks_assigned.toFloat() else 0f
                )
            }
            Result.success(performance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
