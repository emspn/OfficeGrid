package com.app.officegrid.auth.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthDataSource @Inject constructor(
    private val auth: Auth?,
    private val postgrest: Postgrest?
) {
    suspend fun login(email: String, password: String): UserInfo {
        val authPlugin = auth ?: throw Exception("Auth not initialized")
        try {
            authPlugin.signInWith(Email) {
                this.email = email
                this.password = password
            }
            return authPlugin.currentUserOrNull() ?: throw Exception("Login failed")
        } catch (e: Exception) {
            throw Exception(cleanErrorMessage(e.message))
        }
    }

    suspend fun isCompanyIdValid(companyId: String): Boolean {
        val db = postgrest ?: return false
        return try {
            val sanitizedId = companyId.trim().uppercase()
            val response = db.from("organisations")
                .select(columns = Columns.list("id")) {
                    filter { eq("id", sanitizedId) }
                }
            response.data.trim() != "[]"
        } catch (e: Exception) {
            false
        }
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
        val authPlugin = auth ?: throw Exception("Supabase failure")
        val db = postgrest ?: throw Exception("Database failure")
        val sanitizedId = companyId.trim().uppercase()
        
        try {
            // 1. PRE-FLIGHT CHECK: For Admins, ensure ID is UNIQUE
            if (role == "ADMIN") {
                if (isCompanyIdValid(sanitizedId)) {
                    throw Exception("ORG_ID_ALREADY_EXISTS")
                }
            }

            // 2. PRE-FLIGHT CHECK: For Employees, ensure ID EXISTS
            if (role == "EMPLOYEE") {
                if (!isCompanyIdValid(sanitizedId)) {
                    throw Exception("INVALID_ORG_ID")
                }
            }

            // 3. AUTH_INITIALIZATION
            authPlugin.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("role", role)
                    put("company_id", sanitizedId)
                    put("full_name", fullName)
                    orgName?.let { put("organisation_name", it) }
                }
            }

            val user = authPlugin.currentUserOrNull() ?: throw Exception("IDENTITY_FAILURE")

            // 4. WORKSPACE_REGISTRATION
            if (role == "ADMIN" && orgName != null && orgType != null) {
                try {
                    db.from("organisations").insert(
                        buildJsonObject {
                            put("id", sanitizedId)
                            put("name", orgName)
                            put("type", orgType)
                            put("admin_id", user.id)
                        }
                    )
                } catch (e: Exception) {
                    // CRITICAL: Cleanup user if DB insert fails
                    authPlugin.signOut()
                    throw Exception("WORKSPACE_SETUP_FAILURE: ${e.message}")
                }
            }

            // 5. FINAL_NODE_LINKING
            db.from("employees").update(
                buildJsonObject { put("company_id", sanitizedId) }
            ) {
                filter { eq("id", user.id) }
            }

            return user
        } catch (e: Exception) {
            throw Exception(cleanErrorMessage(e.message))
        }
    }

    private fun cleanErrorMessage(message: String?): String {
        if (message == null) return "Unknown Error"
        val msg = message.lowercase()
        return when {
            msg.contains("org_id_already_exists") -> "Organisation ID is already taken. Choose another."
            msg.contains("invalid_org_id") -> "The Organisation ID provided does not exist."
            msg.contains("already registered") -> "Email is already in use."
            msg.contains("workspace_setup_failure") -> "User created but workspace registration failed."
            else -> message
        }
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