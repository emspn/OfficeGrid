package com.app.officegrid.auth.data.repository

import com.app.officegrid.auth.data.remote.SupabaseAuthDataSource
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.auth.domain.model.UserSession
import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.UserRole
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: SupabaseAuthDataSource,
    private val postgrest: Postgrest
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val userInfo = remoteDataSource.login(email, password)
            Result.success(UserSession(user = userInfo.toDomain(), token = ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signup(
        email: String,
        password: String,
        fullName: String,
        role: UserRole,
        companyId: String,
        companyName: String?,
        orgType: String?
    ): Result<User> {
        return try {
            val userInfo = remoteDataSource.signUp(
                email = email,
                password = password,
                role = role.name,
                companyId = companyId,
                fullName = fullName,
                orgName = companyName,
                orgType = orgType
            )
            Result.success(userInfo.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            remoteDataSource.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return remoteDataSource.getSessionStatus().map { status ->
            when (status) {
                is io.github.jan.supabase.auth.status.SessionStatus.Authenticated -> {
                    val userInfo = remoteDataSource.getCurrentUserInfo()
                    userInfo?.toDomain()
                }
                else -> null
            }
        }
    }

    override suspend fun getSession(): UserSession? {
        val userInfo = remoteDataSource.getCurrentUserInfo()
        return userInfo?.let { UserSession(it.toDomain(), "") }
    }

    override suspend fun updateActiveCompany(companyId: String): Result<Unit> {
        return try {
            val user = remoteDataSource.getCurrentUserInfo() ?: throw Exception("Not logged in")
            // Updated to use 'employees' table instead of non-existent 'profiles'
            postgrest["employees"].update(
                mapOf("company_id" to companyId)
            ) {
                filter { eq("id", user.id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun UserInfo.toDomain(): User {
        val rawData = userMetadata
        val roleStr = rawData?.get("role")?.toString()?.removeSurrounding("\"")
        val companyId = rawData?.get("company_id")?.toString()?.removeSurrounding("\"") ?: ""
        val fullName = rawData?.get("full_name")?.toString()?.removeSurrounding("\"") ?: "User"
        
        val role = if (roleStr?.uppercase() == "ADMIN") UserRole.ADMIN else UserRole.EMPLOYEE
        
        // Admins are always approved by the DB trigger
        // Employees status should ideally be fetched from the 'employees' table for real-time accuracy,
        // but for the initial domain mapping from Auth, we check the metadata.
        val isApproved = if (role == UserRole.ADMIN) true 
                         else rawData?.get("is_approved")?.toString()?.toBoolean() ?: false

        return User(
            id = id,
            email = email ?: "",
            fullName = fullName,
            role = role,
            companyId = companyId,
            // Changed from 'organisation_name' to 'org_name' to match SupabaseAuthDataSource and DB Trigger
            companyName = rawData?.get("org_name")?.toString()?.removeSurrounding("\""),
            isApproved = isApproved
        )
    }
}
