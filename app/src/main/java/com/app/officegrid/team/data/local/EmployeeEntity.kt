package com.app.officegrid.team.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.officegrid.team.domain.model.EmployeeStatus

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String,
    val companyId: String,
    val status: EmployeeStatus
)