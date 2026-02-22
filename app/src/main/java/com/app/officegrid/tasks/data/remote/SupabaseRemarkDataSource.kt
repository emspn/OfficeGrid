package com.app.officegrid.tasks.data.remote

import com.app.officegrid.tasks.data.remote.dto.TaskRemarkDto
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

sealed class RemarkRealtimeEvent {
    data class Inserted(val remark: TaskRemarkDto) : RemarkRealtimeEvent()
    data class Deleted(val remarkId: String) : RemarkRealtimeEvent()
}

@Singleton
class SupabaseRemarkDataSource @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getRemarksForTask(taskId: String): List<TaskRemarkDto> {
        return postgrest["task_remarks"]
            .select {
                filter {
                    eq("task_id", taskId)
                }
            }
            .decodeList<TaskRemarkDto>()
    }

    suspend fun insertRemark(remark: TaskRemarkDto) {
        postgrest["task_remarks"].insert(remark)
    }

    /**
     * ⚡ REALTIME REMARKS
     */
    fun observeRemarks(taskId: String): Flow<RemarkRealtimeEvent> {
        val channel = realtime.channel("remarks_$taskId")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "task_remarks"
        }.onStart {
            channel.subscribe()
        }.map { action ->
            when (action) {
                is PostgresAction.Insert -> {
                    val remark = json.decodeFromJsonElement<TaskRemarkDto>(action.record)
                    RemarkRealtimeEvent.Inserted(remark)
                }
                is PostgresAction.Delete -> {
                    val id = action.oldRecord["id"].toString().removeSurrounding("\"")
                    RemarkRealtimeEvent.Deleted(id)
                }
                else -> throw Exception("Unknown realtime action")
            }
        }
    }
}
