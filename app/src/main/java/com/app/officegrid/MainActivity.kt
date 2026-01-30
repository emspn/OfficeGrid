package com.app.officegrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.app.officegrid.auth.presentation.WaitingApprovalScreen
import com.app.officegrid.core.common.SessionManager
import com.app.officegrid.core.common.UserRole
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
                val navController = rememberNavController()
                var isCheckingSession by remember { mutableStateOf(true) }

                // Check for existing session on launch
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(500) // Small delay to let Supabase check session
                    isCheckingSession = false
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isCheckingSession -> {
                            // Show loading while checking for existing session
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        !sessionState.isLoggedIn -> {
                            RootNavGraph(navController = navController)
                        }
                        !sessionState.isApproved -> {
                            WaitingApprovalScreen()
                        }
                        sessionState.userRole == UserRole.ADMIN -> {
                            AdminMainScreen()
                        }
                        sessionState.userRole == UserRole.EMPLOYEE -> {
                            EmployeeMainScreen()
                        }
                    }
                }
            }
        }
    }
}