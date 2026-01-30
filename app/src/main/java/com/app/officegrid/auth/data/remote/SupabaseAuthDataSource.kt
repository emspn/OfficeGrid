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
        val authPlugin = auth ?: throw Exception("Supabase not initialized. Check your internet connection.")
        try {
            android.util.Log.d("SupabaseAuth", "Attempting login for: $email")
            authPlugin.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = authPlugin.currentUserOrNull()
                ?: throw Exception("Login failed. Please check your credentials.")
            android.util.Log.d("SupabaseAuth", "Login successful for: $email")
            return user
        } catch (e: Exception) {
            android.util.Log.e("SupabaseAuth", "Login failed: ${e.message}", e)
            when {
                e.message?.contains("timeout", ignoreCase = true) == true -> {
                    throw Exception("Connection timeout. Please check your internet connection and try again.")
                }
                e.message?.contains("Invalid", ignoreCase = true) == true -> {
                    throw Exception("Invalid email or password")
                }
                else -> throw Exception(cleanErrorMessage(e.message))
            }
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
        val authPlugin = auth ?: throw Exception("Supabase not initialized. Check your internet connection.")
        val db = postgrest ?: throw Exception("Database not initialized. Check your internet connection.")
        val sanitizedId = companyId.trim().uppercase()
        val isApproved = role == "ADMIN"
        
        try {
            android.util.Log.d("SupabaseAuth", "Starting signup for $email, role: $role")

            // 1. PRE-FLIGHT CHECK
            if (role == "ADMIN") {
                android.util.Log.d("SupabaseAuth", "Checking if company ID exists: $sanitizedId")
                if (isCompanyIdValid(sanitizedId)) {
                    throw Exception("ORG_ID_ALREADY_EXISTS")
                }
            } else if (role == "EMPLOYEE") {
                android.util.Log.d("SupabaseAuth", "Validating company ID: $sanitizedId")
                if (!isCompanyIdValid(sanitizedId)) {
                    throw Exception("INVALID_ORG_ID")
                }
            }

            // 2. AUTH_INITIALIZATION (Auth Service)
            android.util.Log.d("SupabaseAuth", "Creating auth account...")
            try {
                authPlugin.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("role", role)
                        put("company_id", sanitizedId)
                        put("full_name", fullName)
                        put("is_approved", isApproved)
                        orgName?.let { put("organisation_name", it) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseAuth", "Auth signup failed: ${e.message}", e)
                throw Exception("Connection timeout. Please check your internet and try again.")
            }

            val user = authPlugin.currentUserOrNull() ?: throw Exception("IDENTITY_FAILURE")
            android.util.Log.d("SupabaseAuth", "Auth account created: ${user.id}")

            // 3. WORKSPACE_REGISTRATION (Only for Admins)
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
                    // Cleanup if DB fails to keep Auth/DB in sync
                    authPlugin.signOut() 
                    throw Exception("WORKSPACE_SETUP_FAILURE: ${e.message}")
                }
            }

            // 4. PROFILE_UPSERT (Link Auth User to Database Profile)
            try {
                db.from("employees").upsert(
                    buildJsonObject {
                        put("id", user.id)
                        put("email", email)
                        put("name", fullName)  // Changed from full_name to name
                        put("role", role)
                        put("company_id", sanitizedId)
                        put("is_approved", isApproved)
                    }
                )
            } catch (e: Exception) {
                // If profile upsert fails, don't block signup - trigger will handle it
                android.util.Log.e("SupabaseAuth", "Profile upsert failed: ${e.message}")
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
