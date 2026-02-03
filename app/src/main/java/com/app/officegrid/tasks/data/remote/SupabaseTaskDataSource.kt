package com.app.officegrid.tasks.data.remote

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class TaskDto(
    val id: String? = null,
    val title: String,
    val description: String = "",
    val status: String = "TODO",
    val priority: String = "MEDIUM",
    val assigned_to: String,
    val created_by: String,
    val company_id: String,
    val due_date: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

sealed class TaskRealtimeEvent {
    data class Inserted(val task: TaskDto) : TaskRealtimeEvent()
    data class Updated(val task: TaskDto) : TaskRealtimeEvent()
    data class Deleted(val taskId: String) : TaskRealtimeEvent()
}

@Singleton
class SupabaseTaskDataSource @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getTasks(companyId: String): List<TaskDto> {
        return postgrest["tasks"]
            .select {
                filter {
                    eq("company_id", companyId)
                }
            }
            .decodeList<TaskDto>()
    }

    suspend fun getTaskById(taskId: String): TaskDto? {
        return postgrest["tasks"]
            .select {
                filter {
                    eq("id", taskId)
                }
            }
            .decodeSingleOrNull<TaskDto>()
    }

    suspend fun createTask(task: TaskDto) {
        postgrest["tasks"].insert(task)
    }

    suspend fun updateTask(task: TaskDto) {
        postgrest["tasks"].update(task) {
            filter { eq("id", task.id ?: "") }
        }
    }

    suspend fun updateTaskStatus(taskId: String, status: String) {
        postgrest["tasks"].update({
            set("status", status)
        }) {
            filter { eq("id", taskId) }
        }
    }

    suspend fun deleteTask(taskId: String) {
        postgrest["tasks"].delete {
            filter { eq("id", taskId) }
        }
    }

    /**
     * ⚡ REALTIME ENGINE (Multi-Node Aware)
     */
    fun observeTasks(companyId: String): Flow<TaskRealtimeEvent> {
        val channel = realtime.channel("tasks_$companyId")
        
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "tasks"
        }.onStart {
            channel.subscribe() // 🚀 CRITICAL: Must subscribe to start receiving data
        }.map { action ->
            when (action) {
                is PostgresAction.Insert -> {
                    val task = json.decodeFromJsonElement<TaskDto>(action.record)
                    TaskRealtimeEvent.Inserted(task)
                }
                is PostgresAction.Update -> {
                    val task = json.decodeFromJsonElement<TaskDto>(action.record)
                    TaskRealtimeEvent.Updated(task)
                }
                is PostgresAction.Delete -> {
                    val id = action.oldRecord["id"].toString().removeSurrounding("\"")
                    TaskRealtimeEvent.Deleted(id)
                }
                else -> throw Exception("Unknown realtime action")
            }
        }
    }
}
