package com.app.officegrid.tasks.data.remote

import com.app.officegrid.tasks.data.remote.dto.TaskDto
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

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
     * ⚡ REALTIME ENGINE (Safe Version)
     * Fixed the 'filter' visibility issue by filtering on the client side.
     */
    fun observeTasks(companyId: String): Flow<TaskRealtimeEvent> {
        val channel = realtime.channel("tasks_registry_$companyId")
        
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "tasks"
        }

        return changeFlow.onStart {
            channel.subscribe()
            Timber.d("REALTIME: Subscribed to missions for registry $companyId")
        }.mapNotNull { action ->
            try {
                when (action) {
                    is PostgresAction.Insert -> {
                        val task = json.decodeFromJsonElement<TaskDto>(action.record)
                        if (task.company_id == companyId) TaskRealtimeEvent.Inserted(task) else null
                    }
                    is PostgresAction.Update -> {
                        val task = json.decodeFromJsonElement<TaskDto>(action.record)
                        if (task.company_id == companyId) TaskRealtimeEvent.Updated(task) else null
                    }
                    is PostgresAction.Delete -> {
                        val id = action.oldRecord["id"]?.toString()?.removeSurrounding("\"") ?: ""
                        if (id.isNotBlank()) TaskRealtimeEvent.Deleted(id) else null
                    }
                    else -> null
                }
            } catch (e: Exception) {
                Timber.e(e, "REALTIME: Failed to parse mission action")
                null
            }
        }
    }
}
