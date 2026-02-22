package com.app.officegrid.auth.domain.repository

import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.auth.domain.model.UserSession
import com.app.officegrid.core.common.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun signup(
        email: String, 
        password: String, 
        fullName: String,
        role: UserRole,
        companyId: String,
        companyName: String? = null,
        orgType: String? = null
    ): Result<User>
    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): Flow<User?>
    suspend fun getSession(): UserSession?
    
    /**
     * Updates the user's active company ID in the database for RLS filtering
     */
    suspend fun updateActiveCompany(companyId: String): Result<Unit>
}
