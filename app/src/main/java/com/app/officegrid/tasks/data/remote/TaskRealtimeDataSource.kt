package com.app.officegrid.tasks.data.remote

import com.app.officegrid.tasks.data.remote.dto.TaskDto
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

sealed class TaskRealtimeEvent {
    data class TaskInserted(val task: TaskDto) : TaskRealtimeEvent()
    data class TaskUpdated(val task: TaskDto) : TaskRealtimeEvent()
    data class TaskDeleted(val taskId: String) : TaskRealtimeEvent()
}

@Singleton
class TaskRealtimeDataSource @Inject constructor(
    private val realtime: Realtime?
) {
    fun subscribeToTaskInserts(): Flow<TaskDto> {
        val realtimePlugin = realtime ?: throw Exception("Supabase Realtime not initialized")

        val channel = realtimePlugin.channel("tasks_inserts")

        return channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "tasks"
        }.map { action ->
            Json.decodeFromJsonElement<TaskDto>(action.record)
        }
    }

    fun subscribeToTaskUpdates(): Flow<TaskDto> {
        val realtimePlugin = realtime ?: throw Exception("Supabase Realtime not initialized")

        val channel = realtimePlugin.channel("tasks_updates")

        return channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "tasks"
        }.map { action ->
            Json.decodeFromJsonElement<TaskDto>(action.record)
        }
    }

    fun subscribeToTaskDeletes(): Flow<String> {
        val realtimePlugin = realtime ?: throw Exception("Supabase Realtime not initialized")

        val channel = realtimePlugin.channel("tasks_deletes")

        return channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
            table = "tasks"
        }.map { action ->
            action.oldRecord["id"]?.toString() ?: ""
        }
    }
}
