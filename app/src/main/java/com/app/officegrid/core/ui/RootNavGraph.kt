package com.app.officegrid.core.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.officegrid.auth.presentation.LoginScreen
import com.app.officegrid.auth.presentation.SignupScreen
import com.app.officegrid.auth.presentation.SignupRoleScreen
import com.app.officegrid.auth.presentation.WelcomeScreen
import com.app.officegrid.core.common.UserRole

@Composable
fun RootNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToSignup = {
                    navController.navigate(Screen.SignupRole.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }
        
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToSignup = {
                    navController.navigate(Screen.SignupRole.route)
                }
            )
        }

        composable(route = Screen.SignupRole.route) {
            SignupRoleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCompanySignup = {
                    navController.navigate(Screen.CompanySignup.route)
                },
                onNavigateToEmployeeSignup = {
                    navController.navigate(Screen.EmployeeSignup.route)
                }
            )
        }

        composable(route = Screen.CompanySignup.route) {
            SignupScreen(
                role = UserRole.ADMIN,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                }
            )
        }

        composable(route = Screen.EmployeeSignup.route) {
            SignupScreen(
                role = UserRole.EMPLOYEE,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                }
            )
        }
    }
}
