package com.app.officegrid.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.officegrid.core.common.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Only start the sentinel if the user was previously logged in
            if (sessionManager.sessionState.value.isLoggedIn) {
                OfficeGridNotificationService.start(context)
            }
        }
    }
}
