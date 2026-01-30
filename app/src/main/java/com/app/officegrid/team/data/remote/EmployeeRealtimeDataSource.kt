package com.app.officegrid.team.data.remote

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
class EmployeeRealtimeDataSource @Inject constructor(
    private val realtime: Realtime?
) {
    fun subscribeToEmployeeInserts(): Flow<EmployeeDto> {
        val realtimePlugin = realtime ?: throw Exception("Supabase Realtime not initialized")

        val channel = realtimePlugin.channel("employees_inserts")

        return channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "employees"
        }.map { action ->
            Json.decodeFromJsonElement<EmployeeDto>(action.record)
        }
    }

    fun subscribeToEmployeeUpdates(): Flow<EmployeeDto> {
        val realtimePlugin = realtime ?: throw Exception("Supabase Realtime not initialized")

        val channel = realtimePlugin.channel("employees_updates")

        return channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "employees"
        }.map { action ->
            Json.decodeFromJsonElement<EmployeeDto>(action.record)
        }
    }

    fun subscribeToEmployeeDeletes(): Flow<String> {
        val realtimePlugin = realtime ?: throw Exception("Supabase Realtime not initialized")

        val channel = realtimePlugin.channel("employees_deletes")

        return channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
            table = "employees"
        }.map { action ->
            action.oldRecord["id"]?.toString() ?: ""
        }
    }
}
