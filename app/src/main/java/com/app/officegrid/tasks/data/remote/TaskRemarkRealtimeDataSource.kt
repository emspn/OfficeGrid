package com.app.officegrid.tasks.data.remote

import com.app.officegrid.tasks.data.remote.dto.TaskRemarkDto
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRemarkRealtimeDataSource @Inject constructor(
    private val realtime: Realtime
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun subscribeToRemarkInserts(): Flow<TaskRemarkDto> {
        val channel = realtime.channel("task_remarks_inserts")
        
        // Define flow BEFORE subscription to avoid IllegalStateException
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "task_remarks"
        }

        return changeFlow.onStart {
            channel.subscribe()
            Timber.d("REALTIME: Subscribed to task_remarks inserts")
        }.map { action ->
            json.decodeFromJsonElement<TaskRemarkDto>(action.record)
        }
    }

    fun subscribeToRemarkDeletes(): Flow<String> {
        val channel = realtime.channel("task_remarks_deletes")

        val changeFlow = channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
            table = "task_remarks"
        }

        return changeFlow.onStart {
            channel.subscribe()
            Timber.d("REALTIME: Subscribed to task_remarks deletes")
        }.map { action ->
            action.oldRecord["id"]?.toString()?.removeSurrounding("\"") ?: ""
        }
    }
}
