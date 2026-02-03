package com.app.officegrid.employee.presentation.workspace_detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.officegrid.employee.presentation.common.EmployeeTopBar
import com.app.officegrid.ui.theme.*

/**
 * ✨ WORKSPACE DETAIL SCREEN
 * Optimized container for Workspace-specific content (Tasks & Dashboard)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceDetailScreen(
    workspaceId: String,
    onNavigateBack: () -> Unit,
    viewModel: WorkspaceDetailViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val workspaceName by viewModel.workspaceName.collectAsState()

    LaunchedEffect(workspaceId) {
        viewModel.loadWorkspaceName(workspaceId)
    }

    // Screen routes that should show the bottom nav
    val rootRoutes = listOf("tasks", "dashboard")
    val showBars = currentDestination?.route in rootRoutes

    Scaffold(
        containerColor = WarmBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // CRITICAL: Reclaims top/bottom system space
        topBar = {
            if (showBars) {
                EmployeeTopBar(
                    title = workspaceName?.uppercase() ?: "LOADING...",
                    onBackClick = onNavigateBack
                )
            }
        },
        bottomBar = {
            if (showBars) {
                // Surface provides the border and background without extra padding
                Surface(
                    color = Color.White,
                    tonalElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder)
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0), // Reclaims bottom wasted space
                        modifier = Modifier.height(64.dp) // Professional tight height
                    ) {
                        val items = listOf(
                            WorkspaceNavItem("Tasks", "tasks", Icons.Default.Task),
                            WorkspaceNavItem("Dashboard", "dashboard", Icons.Default.Dashboard)
                        )

                        items.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(22.dp)) },
                                label = { 
                                    Text(
                                        item.title.uppercase(), 
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    ) 
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepCharcoal,
                                    unselectedIconColor = StoneGray,
                                    selectedTextColor = DeepCharcoal,
                                    unselectedTextColor = StoneGray,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // No extra padding here, child screens handle their own internal padding
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = "tasks",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("tasks") {
                    com.app.officegrid.employee.presentation.tasks.EmployeeTaskListScreen(
                        workspaceId = workspaceId,
                        onTaskClick = { taskId -> navController.navigate("task_detail/$taskId") }
                    )
                }

                composable("dashboard") {
                    com.app.officegrid.employee.presentation.dashboard.EmployeeDashboardScreen(
                        workspaceId = workspaceId,
                        onTaskClick = { taskId -> navController.navigate("task_detail/$taskId") }
                    )
                }

                composable(
                    route = "task_detail/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
                    com.app.officegrid.employee.presentation.task_detail.EmployeeTaskDetailScreen(
                        taskId = taskId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

private data class WorkspaceNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)
