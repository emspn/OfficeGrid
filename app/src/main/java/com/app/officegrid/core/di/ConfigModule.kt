package com.app.officegrid.core.di

import com.app.officegrid.BuildConfig
import com.app.officegrid.core.network.AppEnvironment
import com.app.officegrid.core.network.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideAppEnvironment(): AppEnvironment {
        // In production, this would likely be determined by BuildConfig.FLAVOR or BUILD_TYPE
        return AppEnvironment.DEV
    }

    @Provides
    @Singleton
    fun provideSupabaseConfig(environment: AppEnvironment): SupabaseConfig {
        // All environments now pull from BuildConfig to ensure no keys are in source code.
        // For distinct environments, use Gradle flavors or separate local.properties entries.
        return SupabaseConfig(
            url = BuildConfig.SUPABASE_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY
        )
    }
}
