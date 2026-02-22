package com.app.officegrid.auth.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton
import java.net.UnknownHostException
import java.net.ConnectException

@Singleton
class SupabaseAuthDataSource @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) {
    suspend fun login(email: String, password: String): UserInfo {
        try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            return auth.currentUserOrNull() ?: throw Exception("Login failed. Please try again.")
        } catch (e: Exception) {
            handleException(e)
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
        try {
            val normalizedCompanyId = companyId.trim().uppercase()
            
            // 🚀 STEP 1: Auth SignUp
            // We pass all metadata here. The Supabase Trigger (handle_new_user_signup) 
            // will catch this and automatically create the Org and Employee records.
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("role", role)
                    put("full_name", fullName)
                    put("company_id", normalizedCompanyId)
                    put("org_name", orgName ?: "My Organisation")
                    put("org_type", orgType ?: "Tech")
                }
            }

            // Return the user. The app will react to the session change.
            return auth.currentUserOrNull() ?: throw Exception("Account created, but session could not be started.")
            
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun handleException(e: Exception): Nothing {
        val message = e.message ?: ""
        throw when {
            e is UnknownHostException || e is ConnectException || message.contains("network", true) -> 
                Exception("No internet connection. Please check your network.")
            
            message.contains("Invalid login credentials", true) -> 
                Exception("Invalid email or password.")
            
            message.contains("User already registered", true) || message.contains("already in use", true) -> 
                Exception("This email address is already registered.")
            
            message.contains("duplicate key", true) && message.contains("organisations_pkey") -> 
                Exception("This Workspace ID is already taken. Please try another.")

            else -> Exception(message.ifBlank { "An unexpected error occurred." })
        }
    }

    suspend fun logout() = auth.signOut()
    fun getCurrentUserInfo(): UserInfo? = auth.currentUserOrNull()
    fun getSessionStatus(): Flow<SessionStatus> = auth.sessionStatus
}
