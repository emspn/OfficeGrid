package com.app.officegrid.tasks.data.remote

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

@Singleton
class TaskRemarkRealtimeDataSource @Inject constructor(
    private val realtime: Realtime?
) {
    fun subscribeToRemarkInserts(): Flow<TaskRemarkDto> {
        val realtimePlugin = realtime ?: throw Exception("Supabase Realtime not initialized")

        val channel = realtimePlugin.channel("task_remarks_inserts")

        return channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "task_remarks"
        }.map { action ->
            Json.decodeFromJsonElement<TaskRemarkDto>(action.record)
        }
    }

    fun subscribeToRemarkDeletes(): Flow<String> {
        val realtimePlugin = realtime ?: throw Exception("Supabase Realtime not initialized")

        val channel = realtimePlugin.channel("task_remarks_deletes")

        return channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
            table = "task_remarks"
        }.map { action ->
            action.oldRecord["id"]?.toString() ?: ""
        }
    }
}
