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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(config: SupabaseConfig): SupabaseClient? {
        return try {
            if (config.url.isBlank() || !config.url.startsWith("https")) {
                return null
            }
            
            createSupabaseClient(
                supabaseUrl = config.url,
                supabaseKey = config.anonKey
            ) {
                install(Auth)
                install(Postgrest)
                install(Realtime)
            }
        } catch (e: Exception) {
            null
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient?): Auth? = 
        client?.pluginManager?.getPlugin(Auth)

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient?): Postgrest? = 
        client?.pluginManager?.getPlugin(Postgrest)

    @Provides
    @Singleton
    fun provideSupabaseRealtime(client: SupabaseClient?): Realtime? = 
        client?.pluginManager?.getPlugin(Realtime)
}