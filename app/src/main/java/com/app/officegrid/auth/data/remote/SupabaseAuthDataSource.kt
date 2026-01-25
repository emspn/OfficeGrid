package com.app.officegrid.auth.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthDataSource @Inject constructor(
    private val auth: Auth?
) {
    suspend fun login(email: String, password: String): UserInfo {
        val authPlugin = auth ?: throw Exception("Auth not initialized. Check Supabase configuration.")
        authPlugin.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return authPlugin.currentUserOrNull() ?: throw Exception("Login failed: User null")
    }

    suspend fun signUp(
        email: String, 
        password: String, 
        role: String, 
        companyId: String,
        fullName: String,
        orgName: String? = null,
        orgType: String? = null
    ): UserInfo {
        val authPlugin = auth ?: throw Exception("Auth not initialized. Check Supabase configuration.")
        authPlugin.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("role", role)
                put("company_id", companyId)
                put("full_name", fullName)
                orgName?.let { put("organisation_name", it) }
                orgType?.let { put("organisation_type", it) }
            }
        }
        return authPlugin.currentUserOrNull() ?: throw Exception("Sign up failed: User null")
    }

    suspend fun logout() {
        auth?.signOut()
    }

    fun getCurrentUserInfo(): UserInfo? {
        return auth?.currentUserOrNull()
    }

    fun getSessionStatus(): Flow<SessionStatus> {
        return auth?.sessionStatus ?: MutableStateFlow<SessionStatus>(SessionStatus.NotAuthenticated(isSignOut = false))
    }
}