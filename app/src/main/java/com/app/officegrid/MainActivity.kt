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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.delay
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
                
                // 🚀 HERO FIX: Local state to hold the splash until BOTH 
                // SessionManager is ready AND the minimum splash time has passed.
                var showSplash by remember { mutableStateOf(true) }

                // Notification Permission Request
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

                // FCM Management
                LaunchedEffect(sessionState.isLoggedIn) {
                    if (sessionState.isLoggedIn) {
                        OfficeGridNotificationService.start(this@MainActivity)
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                lifecycleScope.launch(Dispatchers.IO) {
                                    notificationRepository.registerFCMToken(token)
                                }
                            }
                        }
                    } else {
                        OfficeGridNotificationService.stop(this@MainActivity)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (showSplash) {
                        // Keep the splash screen visible until initialization is complete
                        SplashScreen(
                            onTimeout = {
                                // Only dismiss if session initialization has finished
                                if (!sessionState.isInitializing) {
                                    showSplash = false
                                }
                            }
                        )
                        
                        // Fallback: If splash times out but SessionManager is still slow
                        LaunchedEffect(sessionState.isInitializing) {
                            if (!sessionState.isInitializing) {
                                delay(500) // Small extra buffer for smooth transition
                                showSplash = false
                            }
                        }
                    } else {
                        Crossfade(targetState = sessionState, label = "root_routing") { state ->
                            when {
                                !state.isLoggedIn -> {
                                    val authNavController = rememberNavController()
                                    RootNavGraph(navController = authNavController)
                                }
                                else -> {
                                    when (state.userRole) {
                                        UserRole.ADMIN -> AdminMainScreen()
                                        UserRole.EMPLOYEE -> EmployeeMainScreen()
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
    }
}
