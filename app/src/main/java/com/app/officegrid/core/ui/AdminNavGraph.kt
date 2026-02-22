package com.app.officegrid.core.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.officegrid.core.common.presentation.AuditLogsScreen
import com.app.officegrid.core.common.presentation.NotificationScreen
import com.app.officegrid.dashboard.presentation.DashboardScreen
import com.app.officegrid.profile.presentation.AdminProfileScreen
import com.app.officegrid.tasks.presentation.create_task.CreateTaskScreen
import com.app.officegrid.tasks.presentation.create_task.TaskSuccessScreen
import com.app.officegrid.tasks.presentation.edit_task.EditTaskScreen
import com.app.officegrid.tasks.presentation.task_detail.TaskDetailScreen
import com.app.officegrid.tasks.presentation.task_list.TaskListScreen
import com.app.officegrid.team.presentation.TeamScreen
import com.app.officegrid.settings.presentation.AdminSettingsScreen
import com.app.officegrid.organization.presentation.OrganizationSettingsScreen

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
            TaskListScreen(
                onNavigateToCreateTask = { navController.navigate(Screen.AdminCreateTask.route) },
                onNavigateToEditTask = { taskId ->
                    navController.navigate(Screen.AdminEditTask.createRoute(taskId))
                },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }
        composable(Screen.AdminCreateTask.route) {
            CreateTaskScreen(
                onNavigateToSuccess = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.AdminCreateTask.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 🚀 NEW: Task Success Screen with Ad Slot (PhonePe Style)
        composable(
            route = Screen.AdminTaskSuccess.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("employee") { type = NavType.StringType },
                navArgument("date") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val employee = backStackEntry.arguments?.getString("employee") ?: ""
            val date = backStackEntry.arguments?.getLong("date") ?: 0L
            
            TaskSuccessScreen(
                taskTitle = title,
                assignedToName = employee,
                dueDate = date,
                onDone = {
                    navController.navigate(Screen.AdminTasks.route) {
                        popUpTo(Screen.AdminTasks.route) { inclusive = true }
                    }
                }
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
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
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
            AdminProfileScreen(
                onNavigateToSettings = { navController.navigate(Screen.AdminSettings.route) },
                onNavigateToOrganization = { navController.navigate(Screen.OrganizationSettings.route) },
                onNavigateToAudit = { navController.navigate(Screen.AdminAuditLogs.route) }
            )
        }
        composable(Screen.AdminAuditLogs.route) {
            AuditLogsScreen()
        }
        composable(Screen.AdminSettings.route) {
            AdminSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OrganizationSettings.route) {
            OrganizationSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Notifications.route) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTask = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }
    }
}
