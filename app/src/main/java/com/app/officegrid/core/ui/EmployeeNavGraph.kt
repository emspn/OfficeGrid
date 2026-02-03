package com.app.officegrid.core.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.officegrid.core.common.presentation.NotificationScreen
import com.app.officegrid.employee.presentation.dashboard.EmployeeDashboardScreen
import com.app.officegrid.employee.presentation.tasks.EmployeeTaskListScreen
import com.app.officegrid.profile.presentation.EmployeeProfileScreen
import com.app.officegrid.tasks.presentation.task_detail.TaskDetailScreen

@Composable
fun EmployeeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.EmployeeTasks.route
    ) {
        composable(Screen.EmployeeTasks.route) {
            // Using a placeholder or passing workspaceId if available in backstack
            EmployeeTaskListScreen(
                workspaceId = "", 
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }
        
        composable(Screen.AdminDashboard.route) {
            EmployeeDashboardScreen(
                workspaceId = "",
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) {
            TaskDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { /* Employees cannot edit tasks */ }
            )
        }

        composable(Screen.EmployeeProfile.route) {
            EmployeeProfileScreen(
                onNavigateToSettings = { navController.navigate(Screen.Notifications.route) } // Placeholder
            )
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
