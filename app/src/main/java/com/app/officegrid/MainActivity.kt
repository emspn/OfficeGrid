package com.app.officegrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.app.officegrid.auth.domain.repository.AuthRepository
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

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
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