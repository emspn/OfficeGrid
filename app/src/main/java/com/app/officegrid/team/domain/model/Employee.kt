package com.app.officegrid.team.domain.model

data class Employee(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val companyId: String,
    val status: EmployeeStatus,
    val companyName: String? = null // Added companyName to domain model
)

enum class EmployeeStatus {
    PENDING, APPROVED
}
