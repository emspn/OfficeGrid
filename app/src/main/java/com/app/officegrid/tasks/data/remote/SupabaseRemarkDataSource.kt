package com.app.officegrid.tasks.data.remote

import com.app.officegrid.tasks.domain.model.TaskRemark
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class TaskRemarkDto(
    val id: String? = null,
    val task_id: String,
    val message: String,
    val created_by: String,
    val created_at: String? = null
)

@Singleton
class SupabaseRemarkDataSource @Inject constructor(
    private val postgrest: Postgrest?
) {
    suspend fun getRemarksForTask(taskId: String): List<TaskRemarkDto> {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        return postgrestPlugin["task_remarks"]
            .select {
                filter {
                    eq("task_id", taskId)
                }
            }
            .decodeList<TaskRemarkDto>()
    }

    suspend fun insertRemark(remark: TaskRemarkDto) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin["task_remarks"].insert(remark)
    }
}