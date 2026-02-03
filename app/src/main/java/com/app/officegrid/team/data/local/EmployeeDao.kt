package com.app.officegrid.team.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.officegrid.team.domain.model.EmployeeStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE companyId = :companyId ORDER BY name ASC")
    fun getEmployeesByCompany(companyId: String): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :userId ORDER BY companyId ASC")
    fun getEmployeesByUserId(userId: String): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>)

    @Query("UPDATE employees SET status = :status WHERE id = :employeeId AND companyId = :companyId")
    suspend fun updateEmployeeStatusWithCompany(employeeId: String, companyId: String, status: EmployeeStatus)

    @Query("UPDATE employees SET status = :status WHERE id = :employeeId")
    suspend fun updateEmployeeStatus(employeeId: String, status: EmployeeStatus)

    @Query("UPDATE employees SET role = :role WHERE id = :employeeId AND companyId = :companyId")
    suspend fun updateEmployeeRoleWithCompany(employeeId: String, companyId: String, role: String)

    @Query("UPDATE employees SET role = :role WHERE id = :employeeId")
    suspend fun updateEmployeeRole(employeeId: String, role: String)

    @Query("DELETE FROM employees WHERE id = :employeeId AND companyId = :companyId")
    suspend fun deleteEmployeeFromWorkspace(employeeId: String, companyId: String)

    @Query("DELETE FROM employees WHERE id = :employeeId")
    suspend fun deleteEmployee(employeeId: String)

    @Query("DELETE FROM employees WHERE id = :userId")
    suspend fun deleteEmployeesByUserId(userId: String)

    @Query("DELETE FROM employees WHERE companyId = :companyId")
    suspend fun deleteEmployeesByCompany(companyId: String)

    @Query("SELECT * FROM employees WHERE id = :userId AND companyId = :companyId LIMIT 1")
    suspend fun getEmployeeByUserAndCompany(userId: String, companyId: String): EmployeeEntity?
}
