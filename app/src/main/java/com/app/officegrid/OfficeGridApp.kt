package com.app.officegrid

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class OfficeGridApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber first for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // ✅ CRITICAL PRODUCTION FIX: Explicitly initialize Firebase 
        // to prevent crashes in multi-process or early-access scenarios.
        try {
            FirebaseApp.initializeApp(this)
            Timber.d("🔥 Firebase initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "❌ Firebase initialization failed")
        }
    }
}
