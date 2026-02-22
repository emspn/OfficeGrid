package com.app.officegrid.team.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.app.officegrid.team.domain.model.EmployeeStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE companyId = :companyId ORDER BY name ASC")
    fun getEmployeesByCompany(companyId: String): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :userId ORDER BY companyId ASC")
    fun getEmployeesByUserId(userId: String): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :userId")
    suspend fun getEmployeesByUserIdSync(userId: String): List<EmployeeEntity>

    @Query("SELECT * FROM employees WHERE companyId = :companyId")
    suspend fun getEmployeesByCompanySync(companyId: String): List<EmployeeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>)

    @Query("UPDATE employees SET status = :status WHERE id = :employeeId AND companyId = :companyId")
    suspend fun updateEmployeeStatusWithCompany(employeeId: String, companyId: String, status: EmployeeStatus)

    @Query("DELETE FROM employees WHERE id = :employeeId AND companyId = :companyId")
    suspend fun deleteEmployeeFromWorkspace(employeeId: String, companyId: String)

    @Query("DELETE FROM employees WHERE id = :userId")
    suspend fun deleteEmployeesByUserId(userId: String)

    @Query("DELETE FROM employees WHERE companyId = :companyId")
    suspend fun deleteEmployeesByCompany(companyId: String)

    @Query("SELECT * FROM employees WHERE id = :userId AND companyId = :companyId LIMIT 1")
    suspend fun getEmployeeByUserAndCompany(userId: String, companyId: String): EmployeeEntity?

    /**
     * ✅ SYNC USER WORKSPACES without UI flicker
     */
    @Transaction
    suspend fun syncUserWorkspaces(userId: String, remoteEmployees: List<EmployeeEntity>) {
        val current = getEmployeesByUserIdSync(userId)
        val remoteIds = remoteEmployees.map { it.companyId }.toSet()
        
        // Remove orphans
        current.forEach { 
            if (it.companyId !in remoteIds) {
                deleteEmployeeFromWorkspace(userId, it.companyId)
            }
        }
        
        // Upsert new/updated
        insertEmployees(remoteEmployees)
    }

    /**
     * ✅ SYNC COMPANY EMPLOYEES without UI flicker
     */
    @Transaction
    suspend fun syncCompanyEmployees(companyId: String, remoteEmployees: List<EmployeeEntity>) {
        val current = getEmployeesByCompanySync(companyId)
        val remoteIds = remoteEmployees.map { it.id }.toSet()
        
        // Remove orphans
        current.forEach { 
            if (it.id !in remoteIds) {
                deleteEmployeeFromWorkspace(it.id, companyId)
            }
        }
        
        // Upsert new/updated
        insertEmployees(remoteEmployees)
    }
}
