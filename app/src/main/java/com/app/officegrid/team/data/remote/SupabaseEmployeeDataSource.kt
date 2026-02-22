package com.app.officegrid.team.data.remote

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
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
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class EmployeeDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val company_id: String,
    val is_approved: Boolean = false,
    val status: String = "PENDING"
)

@Serializable
data class OrganisationDto(
    val id: String,
    val name: String
)

sealed class EmployeeRealtimeEvent {
    data class Inserted(val employee: EmployeeDto) : EmployeeRealtimeEvent()
    data class Updated(val employee: EmployeeDto) : EmployeeRealtimeEvent()
    data class Deleted(val employeeId: String, val companyId: String) : EmployeeRealtimeEvent()
}

@Singleton
class SupabaseEmployeeDataSource @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getEmployeesByCompany(companyId: String): List<EmployeeDto> {
        return postgrest["employees"]
            .select {
                filter {
                    eq("company_id", companyId)
                }
            }
            .decodeList<EmployeeDto>()
    }

    suspend fun getEmployeesByUserId(userId: String): List<EmployeeDto> {
        return postgrest["employees"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeList<EmployeeDto>()
    }

    suspend fun updateEmployeeStatus(employeeId: String, workspaceId: String, status: String) {
        postgrest["employees"].update({
            set("status", status)
            set("is_approved", status.uppercase() == "APPROVED")
        }) {
            filter {
                eq("id", employeeId)
                eq("company_id", workspaceId)
            }
        }
    }

    suspend fun updateEmployeeRole(employeeId: String, workspaceId: String, role: String) {
        postgrest["employees"].update({
            set("role", role)
        }) {
            filter {
                eq("id", employeeId)
                eq("company_id", workspaceId)
            }
        }
    }

    suspend fun deleteEmployee(employeeId: String, companyId: String) {
        postgrest["employees"].delete {
            filter {
                eq("id", employeeId)
                eq("company_id", companyId)
            }
        }
    }

    suspend fun validateWorkspaceCode(code: String): Boolean {
        return try {
            val normalizedCode = code.trim().uppercase()
            val result = postgrest["organisations"]
                .select(columns = Columns.raw("id")) {
                    filter {
                        eq("id", normalizedCode)
                    }
                }
                .decodeSingleOrNull<Map<String, String>>()
            result != null
        } catch (e: Exception) {
            Timber.e(e, "Workspace validation failed for $code")
            false
        }
    }

    suspend fun getOrganizationName(companyId: String): String? {
        val normalized = companyId.trim().uppercase()
        return try {
            val result = postgrest["organisations"]
                .select(columns = Columns.raw("id, name")) {
                    filter {
                        eq("id", normalized)
                    }
                }
                .decodeSingleOrNull<OrganisationDto>()
            result?.name
        } catch (e: Exception) {
            null
        }
    }

    suspend fun joinWorkspace(userId: String, userName: String, userEmail: String, companyId: String) {
        val normalizedCompanyId = companyId.trim().uppercase()
        postgrest["employees"].insert(buildJsonObject {
            put("id", userId)
            put("company_id", normalizedCompanyId)
            put("name", userName)
            put("email", userEmail)
            put("role", "EMPLOYEE")
            put("status", "PENDING")
            put("is_approved", false)
        })
    }

    suspend fun getWorkspaceAdminId(companyId: String): String? {
        return try {
            val result = postgrest["organisations"]
                .select(columns = Columns.raw("admin_id")) {
                    filter {
                        eq("id", companyId.trim().uppercase())
                    }
                }
            val data = result.decodeSingleOrNull<Map<String, String>>()
            data?.get("admin_id")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Observe employees in a specific company (used by Admins)
     */
    fun observeEmployees(companyId: String): Flow<EmployeeRealtimeEvent> {
        val channel = realtime.channel("employees_company_$companyId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "employees"
        }

        return changeFlow.onStart {
            channel.subscribe()
            Timber.d("REALTIME: Subscribed to employee changes for company $companyId")
        }.map { action ->
            parseEmployeeAction(action)
        }
    }

    /**
     * Observe memberships for a specific user (used by Employees to see approval status)
     */
    fun observeUserMemberships(userId: String): Flow<EmployeeRealtimeEvent> {
        val channel = realtime.channel("employees_user_$userId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "employees"
        }

        return changeFlow.onStart {
            channel.subscribe()
            Timber.d("REALTIME: Subscribed to user memberships for user $userId")
        }.map { action ->
            parseEmployeeAction(action)
        }
    }

    private fun parseEmployeeAction(action: PostgresAction): EmployeeRealtimeEvent {
        Timber.d("REALTIME: Received action: $action")
        return when (action) {
            is PostgresAction.Insert -> {
                val employee = json.decodeFromJsonElement<EmployeeDto>(action.record)
                EmployeeRealtimeEvent.Inserted(employee)
            }
            is PostgresAction.Update -> {
                val employee = json.decodeFromJsonElement<EmployeeDto>(action.record)
                EmployeeRealtimeEvent.Updated(employee)
            }
            is PostgresAction.Delete -> {
                // ✅ FIX: Extract data from oldRecord for DELETE events
                val id = action.oldRecord["id"]?.toString()?.removeSurrounding("\"") ?: ""
                val cid = action.oldRecord["company_id"]?.toString()?.removeSurrounding("\"") ?: ""
                Timber.d("REALTIME: Parsed DELETE event for employee $id in company $cid")
                EmployeeRealtimeEvent.Deleted(id, cid)
            }
            else -> throw Exception("Unknown metadata event")
        }
    }
}
