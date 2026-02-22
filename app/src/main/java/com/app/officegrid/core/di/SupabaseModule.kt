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
            val sanitizedUrl = config.url.trim().removeSuffix("/")
            val sanitizedKey = config.anonKey.trim().removeSurrounding("\"").removeSurrounding("'")
            
            android.util.Log.d("SupabaseModule", "🚀 Initializing Supabase...")

            createSupabaseClient(
                supabaseUrl = sanitizedUrl,
                supabaseKey = sanitizedKey
            ) {
                install(Auth) {
                    autoLoadFromStorage = true
                    autoSaveToStorage = true
                }
                install(Postgrest)
                install(Realtime)

                httpEngine = OkHttp.create {
                    config {
                        connectTimeout(30, TimeUnit.SECONDS)
                        readTimeout(30, TimeUnit.SECONDS)
                        writeTimeout(30, TimeUnit.SECONDS)
                        retryOnConnectionFailure(true)
                    }

                    addInterceptor { chain ->
                        val original = chain.request()
                        val requestBuilder = original.newBuilder()

                        // ✅ Mandatory header for all Supabase REST requests
                        requestBuilder.header("apikey", sanitizedKey)
                        
                        val request = requestBuilder.build()
                        val response = chain.proceed(request)

                        // 101 is "Switching Protocols" (Websocket), which is NOT an error
                        val isSuccessful = response.isSuccessful || response.code == 101

                        if (!isSuccessful) {
                            val errorBody = try {
                                response.peekBody(1024).string()
                            } catch (e: Exception) {
                                "Unavailable"
                            }
                            android.util.Log.e("SupabaseHTTP", "← ERROR ${response.code} on ${request.url}: $errorBody")
                        } else {
                            android.util.Log.d("SupabaseHTTP", "← ${response.code} OK: ${request.url}")
                        }

                        response
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseModule", "💥 FATAL: ${e.message}", e)
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
