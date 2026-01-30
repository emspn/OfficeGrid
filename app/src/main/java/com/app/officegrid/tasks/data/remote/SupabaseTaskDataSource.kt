package com.app.officegrid.tasks.data.remote

import com.app.officegrid.core.common.UserRole
import com.app.officegrid.tasks.data.remote.dto.TaskDto
import com.app.officegrid.tasks.domain.model.TaskStatus
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseTaskDataSource @Inject constructor(
    private val postgrest: Postgrest?
) : TaskRemoteDataSource {

    override suspend fun getTasks(userId: String, role: UserRole, companyId: String): List<TaskDto> {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        
        return postgrestPlugin.from("tasks").select {
            filter {
                eq("company_id", companyId)
                if (role == UserRole.EMPLOYEE) {
                    eq("assigned_to", userId)
                }
            }
        }.decodeList<TaskDto>()
    }

    override suspend fun getTaskById(taskId: String): TaskDto? {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        return try {
            postgrestPlugin.from("tasks").select {
                filter {
                    eq("id", taskId)
                }
            }.decodeSingleOrNull<TaskDto>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin.from("tasks").update(
            {
                set("status", status.name)
            }
        ) {
            filter {
                eq("id", taskId)
            }
        }
    }

    override suspend fun createTask(task: TaskDto) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin.from("tasks").insert(task)
    }

    override suspend fun updateTask(task: TaskDto) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin.from("tasks").update(task) {
            filter {
                eq("id", task.id)
            }
        }
    }

    override suspend fun deleteTask(taskId: String) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin.from("tasks").delete {
            filter {
                eq("id", taskId)
            }
        }
    }
}
