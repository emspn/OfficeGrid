package com.app.officegrid.auth.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthDataSource @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) {
    suspend fun login(email: String, password: String): UserInfo {
        try {
            android.util.Log.d("SupabaseAuth", "════════════════════════════════════════")
            android.util.Log.d("SupabaseAuth", "🔐 LOGIN ATTEMPT")
            android.util.Log.d("SupabaseAuth", "   Email: $email")
            android.util.Log.d("SupabaseAuth", "   Password length: ${password.length}")
            android.util.Log.d("SupabaseAuth", "════════════════════════════════════════")

            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = auth.currentUserOrNull()
                ?: throw Exception("Login failed. Please check your credentials.")
            android.util.Log.d("SupabaseAuth", "✅ Login successful for: $email")
            return user
        } catch (e: Exception) {
            android.util.Log.e("SupabaseAuth", "════════════════════════════════════════")
            android.util.Log.e("SupabaseAuth", "❌ LOGIN FAILED!")
            android.util.Log.e("SupabaseAuth", "   Exception type: ${e.javaClass.simpleName}")
            android.util.Log.e("SupabaseAuth", "   Message: ${e.message}")
            android.util.Log.e("SupabaseAuth", "   Cause: ${e.cause?.message}")
            android.util.Log.e("SupabaseAuth", "════════════════════════════════════════", e)

            val rawError = e.message ?: "Unknown error"
            val cleanMessage = when {
                rawError.contains("Invalid login credentials", ignoreCase = true) ||
                rawError.contains("Invalid", ignoreCase = true) && rawError.contains("password", ignoreCase = true) ->
                    "Invalid email or password. Please try again."

                rawError.contains("timeout", ignoreCase = true) ||
                rawError.contains("timed out", ignoreCase = true) ->
                    "Connection timeout. Please check your internet and try again."

                rawError.contains("network", ignoreCase = true) ||
                rawError.contains("Unable to resolve host", ignoreCase = true) ->
                    "Network error. Please check your internet connection."

                rawError.contains("http", ignoreCase = true) -> {
                    val parts = rawError.split("http", ignoreCase = true)
                    if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                        parts[0].trim()
                    } else {
                        "Login failed. Please check your connection and try again."
                    }
                }

                else -> cleanErrorMessage(rawError)
            }
            throw Exception(cleanMessage)
        }
    }

    suspend fun isCompanyIdValid(companyId: String): Boolean {
        return try {
            val sanitizedId = companyId.trim().uppercase()
            val response = postgrest.from("organisations")
                .select(columns = Columns.list("id")) {
                    filter { eq("id", sanitizedId) }
                }
            response.data.trim() != "[]"
        } catch (e: Exception) {
            android.util.Log.e("SupabaseAuth", "Error checking company ID: ${e.message}")
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
        val sanitizedId = companyId.trim().uppercase()
        val isApproved = role == "ADMIN"
        
        try {
            android.util.Log.d("SupabaseAuth", "Starting signup for $email, role: $role")

            // 1. PRE-FLIGHT CHECK - Only for ADMIN (employees join workspaces later)
            if (role == "ADMIN") {
                android.util.Log.d("SupabaseAuth", "Admin signup - checking company ID: $sanitizedId")
                android.util.Log.d("SupabaseAuth", "Checking if company ID already exists...")
                if (isCompanyIdValid(sanitizedId)) {
                    throw Exception("Organisation ID '$sanitizedId' is already taken. Please choose another.")
                }
            }
            // ✅ NOTE: Employees don't need company_id validation at signup
            // They join workspaces after signup using workspace codes

            // 2. AUTH_INITIALIZATION (Auth Service)
            android.util.Log.d("SupabaseAuth", "Creating auth account...")
            try {
                auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("role", role)
                        // Only add company_id for ADMIN
                        if (role == "ADMIN") {
                            put("company_id", sanitizedId)
                        }
                        put("full_name", fullName)
                        put("is_approved", isApproved)
                        orgName?.let { put("organisation_name", it) }
                    }
                }
                android.util.Log.d("SupabaseAuth", "Auth account created successfully")
            } catch (e: Exception) {
                android.util.Log.e("SupabaseAuth", "❌ Auth signup failed: ${e.message}", e)

                // Parse error message for user-friendly display
                val errorMessage = when {
                    e.message?.contains("User already registered", ignoreCase = true) == true ->
                        "This email is already registered. Please login instead."
                    e.message?.contains("invalid", ignoreCase = true) == true ->
                        "Invalid email or password format."
                    e.message?.contains("Password", ignoreCase = true) == true ->
                        "Password must be at least 6 characters."
                    e.message?.contains("Email", ignoreCase = true) == true ->
                        "Please enter a valid email address."
                    else -> e.message ?: "Signup failed. Please try again."
                }
                throw Exception(errorMessage)
            }


            val user = auth.currentUserOrNull() ?: throw Exception("Account creation failed. Please try again.")
            android.util.Log.d("SupabaseAuth", "Auth account created: ${user.id}")

            // 3. WORKSPACE_REGISTRATION (Only for Admins)
            if (role == "ADMIN" && orgName != null && orgType != null) {
                try {
                    android.util.Log.d("SupabaseAuth", "Creating organization: $orgName with ID: $sanitizedId")
                    postgrest.from("organisations").insert(
                        buildJsonObject {
                            put("id", sanitizedId)
                            put("name", orgName)
                            put("type", orgType)
                            put("admin_id", user.id)
                        }
                    )
                    android.util.Log.d("SupabaseAuth", "Organization created successfully!")
                } catch (e: Exception) {
                    android.util.Log.e("SupabaseAuth", "Organization creation failed: ${e.message}", e)
                    // Cleanup if DB fails to keep Auth/DB in sync
                    auth.signOut()
                    throw Exception("Failed to create workspace: ${e.message}")
                }
            }

            // 4. PROFILE_UPSERT (Link Auth User to Database Profile)
            // ✅ Only insert into employees table for ADMIN at signup
            // Employees don't have company_id yet - they join workspaces later!
            if (role == "ADMIN") {
                try {
                    android.util.Log.d("SupabaseAuth", "Creating employee profile for admin...")
                    postgrest.from("employees").upsert(
                        buildJsonObject {
                            put("id", user.id)
                            put("email", email)
                            put("name", fullName)
                            put("role", role)
                            put("company_id", sanitizedId)
                            put("is_approved", isApproved)
                        }
                    )
                    android.util.Log.d("SupabaseAuth", "Employee profile created successfully!")
                } catch (e: Exception) {
                    // If profile upsert fails, don't block signup - trigger will handle it
                    android.util.Log.w("SupabaseAuth", "Profile upsert failed (non-critical): ${e.message}")
                }
            }
            // ✅ Employees: No employees table entry at signup
            // They will be added when they join a workspace via code

            return user
        } catch (e: Exception) {
            // Log the actual error before cleaning
            android.util.Log.e("SupabaseAuth", "❌ SIGNUP FAILED!")
            android.util.Log.e("SupabaseAuth", "Exception type: ${e.javaClass.simpleName}")
            android.util.Log.e("SupabaseAuth", "Original message: ${e.message}")
            android.util.Log.e("SupabaseAuth", "Stack trace:", e)

            val cleanedMessage = cleanErrorMessage(e.message)
            android.util.Log.e("SupabaseAuth", "Cleaned message: $cleanedMessage")

            throw Exception(cleanedMessage)
        }
    }

    private fun cleanErrorMessage(message: String?): String {
        if (message == null || message.isBlank()) return "Please check your connection and try again."

        val msg = message.lowercase()

        // Check for specific known errors first
        return when {
            msg.contains("org_id_already_exists") ->
                "Organisation ID is already taken. Please choose another."

            msg.contains("invalid_org_id") ->
                "The Organisation ID provided does not exist."

            msg.contains("already registered") || msg.contains("user already registered") ->
                "This email is already registered. Please login instead."

            msg.contains("workspace_setup_failure") ->
                "Workspace setup failed. Please try again."

            msg.contains("invalid login credentials") ->
                "Invalid email or password."

            msg.contains("network") || msg.contains("unable to resolve host") ->
                "Network error. Please check your internet connection."

            msg.contains("timeout") ->
                "Connection timeout. Please try again."

            // Filter out URLs and technical details
            msg.contains("http://") || msg.contains("https://") -> {
                val cleanPart = message.substringBefore("http://").substringBefore("https://").trim()
                if (cleanPart.isNotBlank() && cleanPart.length > 10) {
                    cleanPart
                } else {
                    "Operation failed. Please check your connection and try again."
                }
            }

            // Generic cleanup - remove technical stack traces
            else -> {
                message
                    .substringBefore("http://")
                    .substringBefore("https://")
                    .substringBefore(" at ")
                    .substringBefore("Exception")
                    .substringBefore("Error:")
                    .trim()
                    .take(100) // Limit message length
                    .ifBlank { "Operation failed. Please try again." }
            }
        }
    }

    suspend fun logout() {
        auth.signOut()
    }

    fun getCurrentUserInfo(): UserInfo? {
        return auth.currentUserOrNull()
    }

    fun getSessionStatus(): Flow<SessionStatus> {
        return auth.sessionStatus
    }
}

