package com.app.officegrid

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.core.common.domain.repository.NotificationRepository
import com.app.officegrid.core.notification.OfficeGridNotificationService
import com.app.officegrid.core.ui.AdminMainScreen
import com.app.officegrid.core.ui.EmployeeMainScreen
import com.app.officegrid.core.ui.RootNavGraph
import com.app.officegrid.core.ui.SplashScreen
import com.app.officegrid.ui.theme.OfficeGridTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var notificationRepository: NotificationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OfficeGridTheme {
                val sessionState by sessionManager.sessionState.collectAsState()
                
                // 1. Notification Permission Request
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted && sessionState.isLoggedIn) {
                            OfficeGridNotificationService.start(this)
                        }
                    }
                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // 2. Lifecycle & FCM Management
                LaunchedEffect(sessionState.isLoggedIn) {
                    if (sessionState.isLoggedIn) {
                        // Start WebSocket Sentinel
                        OfficeGridNotificationService.start(this@MainActivity)
                        
                        // ✅ SAFE PRODUCTION FCM TOKEN SYNC
                        try {
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    task.result?.let { token ->
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            try {
                                                notificationRepository.registerFCMToken(token)
                                                Timber.d("FCM Token synchronized with registry.")
                                            } catch (e: Exception) {
                                                Timber.e(e, "Failed to sync FCM token")
                                            }
                                        }
                                    }
                                } else {
                                    Timber.w("FCM token retrieval failed: ${task.exception?.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Firebase Messaging is not available on this device")
                        }
                    } else {
                        OfficeGridNotificationService.stop(this@MainActivity)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // 🚀 FLICKER PROTECTION: Hold the Splash screen until session state is definitely loaded.
                    if (sessionState.isInitializing) {
                        SplashScreen(onTimeout = {})
                    } else {
                        Crossfade(
                            targetState = sessionState.isLoggedIn to sessionState.userRole,
                            animationSpec = tween(durationMillis = 400),
                            label = "root_routing"
                        ) { (isLoggedIn, role) ->
                            when {
                                !isLoggedIn -> {
                                    val authNavController = rememberNavController()
                                    RootNavGraph(navController = authNavController)
                                }
                                role == UserRole.ADMIN -> AdminMainScreen()
                                role == UserRole.EMPLOYEE -> EmployeeMainScreen()
                                else -> {
                                    val fallbackNavController = rememberNavController()
                                    RootNavGraph(navController = fallbackNavController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
