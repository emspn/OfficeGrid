package com.app.officegrid.core.network

/**
 * Configuration for Supabase environments.
 */
data class SupabaseConfig(
    val url: String,
    val anonKey: String
)

/**
 * Environment selector for the application.
 */
enum class AppEnvironment {
    DEV, STAGING, PROD
}
