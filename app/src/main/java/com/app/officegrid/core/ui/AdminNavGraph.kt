package com.app.officegrid.core.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.officegrid.core.common.presentation.AuditLogsScreen
import com.app.officegrid.dashboard.presentation.DashboardScreen
import com.app.officegrid.profile.presentation.ProfileScreen
import com.app.officegrid.tasks.presentation.create_task.CreateTaskScreen
import com.app.officegrid.tasks.presentation.edit_task.EditTaskScreen
import com.app.officegrid.tasks.presentation.task_detail.TaskDetailScreen
import com.app.officegrid.tasks.presentation.task_list.TaskListScreen
import com.app.officegrid.team.presentation.TeamScreen

@Composable
fun AdminNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.AdminDashboard.route
    ) {
        composable(Screen.AdminDashboard.route) {
            DashboardScreen()
        }
        composable(Screen.AdminTasks.route) {
            TaskListScreen()
        }
        composable(Screen.AdminCreateTask.route) {
            CreateTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AdminEditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) {
            EditTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) {
            TaskDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> 
                    navController.navigate(Screen.AdminEditTask.createRoute(id))
                }
            )
        }
        composable(Screen.AdminTeam.route) {
            TeamScreen()
        }
        composable(Screen.AdminProfile.route) {
            ProfileScreen()
        }
        composable(Screen.AdminAuditLogs.route) {
            AuditLogsScreen()
        }
    }
}