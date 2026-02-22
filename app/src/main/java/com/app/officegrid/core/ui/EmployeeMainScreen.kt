package com.app.officegrid.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.app.officegrid.core.common.presentation.NotificationViewModel
import com.app.officegrid.core.common.presentation.NotificationScreen
import com.app.officegrid.employee.presentation.join_workspace.JoinWorkspaceDialog
import com.app.officegrid.employee.presentation.workspace_list.WorkspaceListScreen
import com.app.officegrid.profile.presentation.EmployeeProfileScreen
import com.app.officegrid.settings.presentation.EmployeeSettingsScreen
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeMainScreen(
    viewModel: com.app.officegrid.employee.presentation.workspace_list.WorkspaceViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showJoinDialog by viewModel.showJoinDialog.collectAsState()
    val unreadNotifications by notificationViewModel.unreadCount.collectAsState()
    val isJoining by viewModel.isJoining.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ CENTRALIZED EVENT COLLECTION: ONLY HERE
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.app.officegrid.employee.presentation.workspace_list.WorkspaceEvent.Success -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is com.app.officegrid.employee.presentation.workspace_list.WorkspaceEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val showBottomBar = currentDestination?.route in listOf("workspaces", "profile")

    Scaffold(
        containerColor = WarmBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showBottomBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "OfficeGrid",
                            style = MaterialTheme.typography.titleLarge.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = DeepCharcoal
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("notifications") }) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifications > 0) {
                                        Badge(
                                            containerColor = ProfessionalError,
                                            contentColor = Color.White
                                        ) {
                                            Text(unreadNotifications.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = DeepCharcoal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmBackground),
                    windowInsets = WindowInsets.statusBars
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = Color.White,
                    border = BorderStroke(1.dp, WarmBorder)
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        val workspacesSelected = currentDestination?.hierarchy?.any {
                            it.route == "workspaces"
                        } == true

                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Business, contentDescription = "Nodes", modifier = Modifier.size(22.dp)) },
                            label = {
                                Text("NODES", style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (workspacesSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp
                                ))
                            },
                            selected = workspacesSelected,
                            onClick = {
                                navController.navigate("workspaces") {
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

                        val profileSelected = currentDestination?.hierarchy?.any {
                            it.route == "profile"
                        } == true

                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(22.dp)) },
                            label = {
                                Text("PROFILE", style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (profileSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp
                                ))
                            },
                            selected = profileSelected,
                            onClick = {
                                navController.navigate("profile") {
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "workspaces",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("workspaces") {
                WorkspaceListScreen(
                    onWorkspaceClick = { workspaceId ->
                        navController.navigate("workspace_detail/$workspaceId")
                    },
                    onAddWorkspace = { viewModel.toggleJoinDialog(true) },
                    onNavigateToProfile = { navController.navigate("profile") },
                    viewModel = viewModel
                )
            }

            composable(
                route = "workspace_detail/{workspaceId}",
                arguments = listOf(navArgument("workspaceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val workspaceId = backStackEntry.arguments?.getString("workspaceId") ?: ""
                com.app.officegrid.employee.presentation.workspace_detail.WorkspaceDetailScreen(
                    workspaceId = workspaceId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("profile") {
                EmployeeProfileScreen(
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("settings") {
                EmployeeSettingsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("notifications") {
                NotificationScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }

    if (showJoinDialog) {
        JoinWorkspaceDialog(
            onDismiss = { viewModel.toggleJoinDialog(false) },
            onJoin = { code ->
                viewModel.joinWorkspace(code)
            },
            isLoading = isJoining
        )
    }
}
