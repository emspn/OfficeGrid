package com.app.officegrid.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.officegrid.core.common.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🚀 PRODUCTION BOOT RECEIVER
 * Ensures background services survive a device restart.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var sessionManager: SessionManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            // ✅ HERO FIX: We must wait for the session to initialize from storage
            // otherwise 'isLoggedIn' will be false by default during boot sequence.
            scope.launch {
                val session = sessionManager.sessionState
                    .filter { !it.isInitializing } // Wait for the loading to finish
                    .first()

                if (session.isLoggedIn) {
                    android.util.Log.d("BootReceiver", "⚡ Recovering session for user: ${session.userId}")
                    OfficeGridNotificationService.start(context)
                } else {
                    android.util.Log.d("BootReceiver", "Registry inactive - skipping service start.")
                }
            }
        }
    }
}
