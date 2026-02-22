package com.app.officegrid.dashboard.data.remote

import com.app.officegrid.analytics.data.remote.dto.AnalyticsDto
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PerformanceDto(
    val employee_id: String,
    val employee_name: String,
    val tasks_assigned: Int,
    val tasks_completed: Int
)

@Singleton
class SupabaseAnalyticsDataSource @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getAnalytics(companyId: String): AnalyticsDto? {
        return postgrest["analytics"]
            .select {
                filter {
                    eq("company_id", companyId)
                }
            }
            .decodeSingleOrNull<AnalyticsDto>()
    }

    /**
     * ✅ Optimized RPC call to get pre-calculated performance data
     */
    suspend fun getTeamPerformance(companyId: String): List<PerformanceDto> {
        return postgrest.rpc(
            function = "get_team_performance",
            parameters = buildJsonObject { put("company_id_param", companyId) }
        ).decodeList<PerformanceDto>()
    }

    /**
     * ⚡ REALTIME ANALYTICS: Observe changes to the analytics table
     */
    fun observeAnalytics(companyId: String): Flow<AnalyticsDto> {
        val channel = realtime.channel("analytics_realtime_$companyId")
        
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "analytics"
        }

        return changeFlow.onStart {
            channel.subscribe()
        }.map { action ->
            when (action) {
                is PostgresAction.Update -> {
                    json.decodeFromJsonElement<AnalyticsDto>(action.record)
                }
                is PostgresAction.Insert -> {
                    json.decodeFromJsonElement<AnalyticsDto>(action.record)
                }
                else -> throw Exception("Unsupported analytics action")
            }
        }
    }
}
