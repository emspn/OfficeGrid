package com.app.officegrid.tasks.data.remote

import com.app.officegrid.core.common.UserRole
import com.app.officegrid.tasks.data.remote.dto.TaskDto
import com.app.officegrid.tasks.domain.model.TaskStatus
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseTaskDataSource @Inject constructor(
    private val postgrest: Postgrest?
) : TaskRemoteDataSource {

    override suspend fun getTasks(userId: String, role: UserRole): List<TaskDto> {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        
        return if (role == UserRole.ADMIN) {
            postgrestPlugin["tasks"].select().decodeList<TaskDto>()
        } else {
            postgrestPlugin["tasks"].select {
                filter {
                    eq("assigned_to", userId)
                }
            }.decodeList<TaskDto>()
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin["tasks"].update({
            set("status", status.name)
        }) {
            filter {
                eq("id", taskId)
            }
        }
    }

    override suspend fun createTask(task: TaskDto) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin["tasks"].insert(task)
    }

    override suspend fun updateTask(task: TaskDto) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin["tasks"].update(task) {
            filter {
                eq("id", task.id)
            }
        }
    }

    override suspend fun deleteTask(taskId: String) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin["tasks"].delete {
            filter {
                eq("id", taskId)
            }
        }
    }
}