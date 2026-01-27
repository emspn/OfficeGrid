package com.app.officegrid.core.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.officegrid.dashboard.presentation.DashboardScreen
import com.app.officegrid.profile.presentation.ProfileScreen
import com.app.officegrid.tasks.presentation.task_detail.TaskDetailScreen
import com.app.officegrid.tasks.presentation.task_list.TaskListScreen

@Composable
fun EmployeeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.EmployeeTasks.route
    ) {
        composable(Screen.EmployeeTasks.route) {
            TaskListScreen()
        }
        // Fix: Added Dashboard route for Employees
        composable(Screen.AdminDashboard.route) {
            DashboardScreen()
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
            ProfileScreen()
        }
    }
}