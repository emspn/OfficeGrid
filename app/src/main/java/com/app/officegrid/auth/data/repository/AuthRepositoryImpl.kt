package com.app.officegrid.auth.data.repository

import com.app.officegrid.auth.data.remote.SupabaseAuthDataSource
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.auth.domain.model.UserSession
import com.app.officegrid.auth.domain.repository.AuthRepository
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.common.UserRole
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: SupabaseAuthDataSource,
    private val sessionManager: SessionManager
) : AuthRepository {

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        observeSessionStatus()
    }

    private fun observeSessionStatus() {
        scope.launch {
            remoteDataSource.getSessionStatus().collectLatest { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = remoteDataSource.getCurrentUserInfo()?.toDomain()
                        user?.let { sessionManager.login(it.role) }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        sessionManager.logout()
                    }
                    else -> Unit
                }
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val userInfo = remoteDataSource.login(email, password)
            val user = userInfo.toDomain()
            Result.success(
                UserSession(
                    user = user,
                    token = ""
                )
            )
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
        return sessionManager.sessionState.map { state ->
            if (state.isLoggedIn && state.userRole != null) {
                val userInfo = remoteDataSource.getCurrentUserInfo()
                userInfo?.toDomain() ?: User("id", "email", state.userRole, "company")
            } else null
        }
    }

    override suspend fun getSession(): UserSession? {
        val userInfo = remoteDataSource.getCurrentUserInfo()
        return userInfo?.let {
            UserSession(it.toDomain(), "")
        }
    }

    private fun UserInfo.toDomain(): User {
        val roleMetadata = userMetadata?.get("role")?.toString()?.removeSurrounding("\"")
        val role = when {
            roleMetadata?.uppercase() == "ADMIN" -> UserRole.ADMIN
            email?.contains("admin") == true -> UserRole.ADMIN
            else -> UserRole.EMPLOYEE
        }
        
        return User(
            id = id,
            email = email ?: "",
            role = role,
            companyId = userMetadata?.get("company_id")?.toString()?.removeSurrounding("\"") ?: ""
        )
    }
}