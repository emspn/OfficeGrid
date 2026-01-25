package com.app.officegrid.team.data.remote

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class EmployeeDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val company_id: String,
    val status: String = "PENDING"
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
        postgrestPlugin["employees"].update({
            set("status", status)
        }) {
            filter {
                eq("id", employeeId)
            }
        }
    }
}