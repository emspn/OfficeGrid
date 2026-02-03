package com.app.officegrid.core.di

import com.app.officegrid.core.network.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(config: SupabaseConfig): SupabaseClient {
        return try {
            // Validate URL
            if (config.url.isBlank()) {
                android.util.Log.e("SupabaseModule", "❌ SUPABASE_URL is EMPTY!")
                throw IllegalStateException("Supabase URL is not configured")
            }

            if (!config.url.startsWith("https://")) {
                android.util.Log.e("SupabaseModule", "❌ Invalid Supabase URL: ${config.url}")
                throw IllegalStateException("Invalid Supabase URL: ${config.url}")
            }

            // Validate anon key - support both old (eyJ...) and new (sb_publishable_...) formats
            if (config.anonKey.isBlank()) {
                android.util.Log.e("SupabaseModule", "❌ SUPABASE_ANON_KEY is EMPTY!")
                throw IllegalStateException("Supabase anon key is not configured")
            }

            android.util.Log.d("SupabaseModule", "✅ Starting Supabase initialization...")
            android.util.Log.d("SupabaseModule", "URL: ${config.url}")
            android.util.Log.d("SupabaseModule", "Key format: ${if (config.anonKey.startsWith("sb_")) "New publishable" else "JWT"}")

            val client = createSupabaseClient(
                supabaseUrl = config.url,
                supabaseKey = config.anonKey
            ) {
                install(Auth) {
                    autoLoadFromStorage = true
                    autoSaveToStorage = true
                }
                install(Postgrest)
                install(Realtime)

                // Configure HTTP client with timeouts
                httpEngine = OkHttp.create {
                    config {
                        connectTimeout(30, TimeUnit.SECONDS)
                        readTimeout(30, TimeUnit.SECONDS)
                        writeTimeout(30, TimeUnit.SECONDS)
                        callTimeout(60, TimeUnit.SECONDS)
                        retryOnConnectionFailure(true)
                    }

                    // Add interceptor to log responses
                    addInterceptor { chain ->
                        val request = chain.request()
                        android.util.Log.d("SupabaseHTTP", "→ ${request.method} ${request.url}")

                        val response = chain.proceed(request)

                        if (!response.isSuccessful) {
                            android.util.Log.e("SupabaseHTTP", "← ${response.code} ${response.message}")
                        } else {
                            android.util.Log.d("SupabaseHTTP", "← ${response.code} OK")
                        }

                        response
                    }
                }
            }

            android.util.Log.d("SupabaseModule", "✅ Supabase client created successfully!")
            client
        } catch (e: Exception) {
            android.util.Log.e("SupabaseModule", "❌ FAILED: ${e.message}", e)
            throw e
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth =
        client.pluginManager.getPlugin(Auth)

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient): Postgrest =
        client.pluginManager.getPlugin(Postgrest)

    @Provides
    @Singleton
    fun provideSupabaseRealtime(client: SupabaseClient): Realtime =
        client.pluginManager.getPlugin(Realtime)
}
