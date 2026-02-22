package com.app.officegrid.team.data.local

import androidx.room.Entity
import com.app.officegrid.team.domain.model.EmployeeStatus

/**
 * Employee entity representing a user's membership in a workspace.
 * An employee can join multiple workspaces, so we use composite primary key (id + companyId).
 */
@Entity(
    tableName = "employees",
    primaryKeys = ["id", "companyId"]
)
data class EmployeeEntity(
    val id: String,           // User ID from auth
    val name: String,
    val email: String,
    val role: String,
    val companyId: String,    // Workspace/Organization ID
    val status: EmployeeStatus,
    val companyName: String? = null // Cached organization name
)
