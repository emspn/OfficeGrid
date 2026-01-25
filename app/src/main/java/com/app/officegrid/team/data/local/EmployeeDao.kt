package com.app.officegrid.team.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE companyId = :companyId ORDER BY name ASC")
    fun getEmployeesByCompany(companyId: String): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>)

    @Query("DELETE FROM employees WHERE companyId = :companyId")
    suspend fun deleteEmployeesByCompany(companyId: String)
}