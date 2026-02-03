package com.app.officegrid

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.core.notification.OfficeGridNotificationService
import com.app.officegrid.core.ui.AdminMainScreen
import com.app.officegrid.core.ui.EmployeeMainScreen
import com.app.officegrid.core.ui.RootNavGraph
import com.app.officegrid.ui.theme.OfficeGridTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OfficeGridTheme {
                val sessionState by sessionManager.sessionState.collectAsState()
                
                // 🚀 HERO FIX: Use rememberSaveable to maintain state across minimize/reopen
                var isFirstLaunch by rememberSaveable { mutableStateOf(true) }
                
                // 🔔 Notification Permission Request
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

                // Handle background service
                LaunchedEffect(sessionState.isLoggedIn) {
                    if (sessionState.isLoggedIn) {
                        OfficeGridNotificationService.start(this@MainActivity)
                    } else {
                        OfficeGridNotificationService.stop(this@MainActivity)
                    }
                }

                // 🔄 Smart Session Check: No fixed delays!
                if (isFirstLaunch && !sessionState.isLoggedIn) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    // Small delay only on cold start to allow Supabase to restore session
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(800)
                        isFirstLaunch = false
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            !sessionState.isLoggedIn -> {
                                val navController = rememberNavController()
                                RootNavGraph(navController = navController)
                            }
                            sessionState.userRole == UserRole.ADMIN -> {
                                AdminMainScreen()
                            }
                            sessionState.userRole == UserRole.EMPLOYEE -> {
                                EmployeeMainScreen()
                            }
                            else -> {
                                val navController = rememberNavController()
                                RootNavGraph(navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}
