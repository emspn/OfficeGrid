package com.app.officegrid.team.data.remote

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
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
    val is_approved: Boolean = false
)

@Singleton
class SupabaseEmployeeDataSource @Inject constructor(
    private val postgrest: Postgrest?
) {
    suspend fun getEmployeesByCompany(companyId: String): List<EmployeeDto> {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        return postgrestPlugin["employees"]
            .select {
                filter {
                    eq("company_id", companyId)
                }
            }
            .decodeList<EmployeeDto>()
    }

    suspend fun updateEmployeeStatus(employeeId: String, status: String) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        val isApproved = status == "APPROVED"

        android.util.Log.d("SupabaseEmployee", "Updating employee $employeeId to status: $status")

        if (isApproved) {
            // Use the function that updates both employees table AND auth metadata
            try {
                postgrestPlugin.rpc("approve_employee", buildJsonObject {
                    put("employee_id", employeeId)
                })
                android.util.Log.d("SupabaseEmployee", "Successfully called approve_employee function")
            } catch (e: Exception) {
                android.util.Log.e("SupabaseEmployee", "RPC call failed, falling back to direct update: ${e.message}")
                // Fallback to direct update if function doesn't exist yet
                postgrestPlugin["employees"].update({
                    set("is_approved", true)
                }) {
                    filter {
                        eq("id", employeeId)
                    }
                }
            }
        } else {
            // For rejection/denial, just update employees table
            postgrestPlugin["employees"].update({
                set("is_approved", false)
            }) {
                filter {
                    eq("id", employeeId)
                }
            }
        }
    }

    suspend fun deleteEmployee(employeeId: String) {
        val postgrestPlugin = postgrest ?: throw Exception("Supabase Postgrest not initialized")
        postgrestPlugin["employees"].delete {
            filter {
                eq("id", employeeId)
            }
        }
    }
}