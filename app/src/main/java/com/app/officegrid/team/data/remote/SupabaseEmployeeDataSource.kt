package com.app.officegrid.team.data.remote

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
data class EmployeeDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val company_id: String,
    val is_approved: Boolean = false,
    val status: String = "PENDING"
)

sealed class EmployeeRealtimeEvent {
    data class Inserted(val employee: EmployeeDto) : EmployeeRealtimeEvent()
    data class Updated(val employee: EmployeeDto) : EmployeeRealtimeEvent()
    data class Deleted(val employeeId: String) : EmployeeRealtimeEvent()
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
        val isApproved = status == "APPROVED"
        if (isApproved) {
            try {
                postgrest.rpc("approve_employee", buildJsonObject {
                    put("employee_id", employeeId)
                    put("workspace_id", workspaceId)
                })
            } catch (e: Exception) {
                postgrest["employees"].update({
                    set("is_approved", true)
                    set("status", "APPROVED")
                }) {
                    filter {
                        eq("id", employeeId)
                        eq("company_id", workspaceId)
                    }
                }
            }
        } else {
            postgrest["employees"].update({
                set("is_approved", false)
                set("status", "REJECTED")
            }) {
                filter {
                    eq("id", employeeId)
                    eq("company_id", workspaceId)
                }
            }
        }
    }

    suspend fun updateEmployeeRole(employeeId: String, workspaceId: String, role: String) {
        try {
            postgrest.rpc("update_operative_role", buildJsonObject {
                put("employee_id", employeeId)
                put("workspace_id", workspaceId)
                put("new_role", role)
            })
        } catch (e: Exception) {
            postgrest["employees"].update({
                set("role", role)
            }) {
                filter {
                    eq("id", employeeId)
                    eq("company_id", workspaceId)
                }
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
        val normalizedCode = code.trim().uppercase()
        return try {
            val org = postgrest["organisations"]
                .select {
                    filter {
                        eq("id", normalizedCode)
                    }
                }
                .decodeSingleOrNull<OrganisationDto>()
            org != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getOrganizationName(companyId: String): String? {
        return try {
            val result = postgrest["organisations"]
                .select {
                    filter {
                        eq("id", companyId)
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
        try {
            // ✅ Trying RPC first
            postgrest.rpc("join_workspace", buildJsonObject {
                put("workspace_code", normalizedCompanyId)
                put("employee_user_id", userId)
                put("employee_name", userName)
                put("employee_email", userEmail)
            })
        } catch (e: Exception) {
            // ✅ FALLBACK: Setting default role to 'OPERATIVE' as requested
            postgrest["employees"].insert(
                buildJsonObject {
                    put("id", userId)
                    put("name", userName)
                    put("email", userEmail)
                    put("role", "OPERATIVE")
                    put("company_id", normalizedCompanyId)
                    put("is_approved", false)
                    put("status", "PENDING")
                }
            )
        }
    }

    suspend fun getWorkspaceAdminId(companyId: String): String? {
        return try {
            val result = postgrest["organisations"]
                .select {
                    filter {
                        eq("id", companyId)
                    }
                }
                .decodeSingleOrNull<OrganisationDto>()
            result?.admin_id
        } catch (e: Exception) {
            null
        }
    }

    /**
     * ⚡ REALTIME ENGINE (Employee Awareness)
     */
    fun observeEmployees(companyId: String): Flow<EmployeeRealtimeEvent> {
        val channel = realtime.channel("employees_$companyId")
        
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "employees"
        }.onStart {
            channel.subscribe() // 🚀 CRITICAL: Start the stream!
        }.map { action ->
            when (action) {
                is PostgresAction.Insert -> {
                    val employee = json.decodeFromJsonElement<EmployeeDto>(action.record)
                    EmployeeRealtimeEvent.Inserted(employee)
                }
                is PostgresAction.Update -> {
                    val employee = json.decodeFromJsonElement<EmployeeDto>(action.record)
                    EmployeeRealtimeEvent.Updated(employee)
                }
                is PostgresAction.Delete -> {
                    val id = action.oldRecord["id"].toString().removeSurrounding("\"")
                    EmployeeRealtimeEvent.Deleted(id)
                }
                else -> throw Exception("Unknown realtime action")
            }
        }
    }
}

@Serializable
data class OrganisationDto(
    val id: String,
    val name: String,
    val type: String? = null,
    val admin_id: String? = null
)
