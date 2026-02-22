package com.app.officegrid.core.ui

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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.officegrid.core.common.presentation.NotificationViewModel
import com.app.officegrid.core.ui.components.OfficeGridLogo
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val unreadNotifications by notificationViewModel.unreadCount.collectAsState()
    val pendingTeamRequests by notificationViewModel.pendingTeamRequests.collectAsState()

    val items = listOf(
        NavigationItem("DASHBOARD", Screen.AdminDashboard.route, Icons.Default.GridView),
        NavigationItem("TASKS", Screen.AdminTasks.route, Icons.Default.Task),
        NavigationItem("AUDIT", Screen.AdminAuditLogs.route, Icons.Default.Terminal),
        NavigationItem("TEAM", Screen.AdminTeam.route, Icons.Default.Groups),
        NavigationItem("PROFILE", Screen.AdminProfile.route, Icons.Default.Person)
    )

    val currentRoute = currentDestination?.route
    val isDetailScreen = currentRoute?.let { route ->
        route.startsWith("admin_create_task") ||
        route.startsWith("admin_edit_task") ||
        route.startsWith("task_detail") ||
        route.startsWith("notifications") ||
        route.startsWith("admin_settings") ||
        route.startsWith("organization_settings") ||
        route.startsWith("admin_task_success") // ✅ FIXED: Mission Success Screen hides all bars
    } ?: false

    Scaffold(
        containerColor = WarmBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (!isDetailScreen) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OfficeGridLogo(size = 24.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "OFFICE_GRID",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                ),
                                color = DeepCharcoal
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifications > 0) {
                                        Badge(containerColor = ProfessionalError, contentColor = Color.White) {
                                            Text(unreadNotifications.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, null, tint = DeepCharcoal, modifier = Modifier.size(24.dp))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmBackground),
                    windowInsets = WindowInsets.statusBars
                )
            }
        },
        bottomBar = {
            if (!isDetailScreen) {
                Surface(
                    color = Color.White,
                    tonalElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, WarmBorder),
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier.height(64.dp)
                    ) {
                        items.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            val isTeamItem = item.title == "TEAM"

                            NavigationBarItem(
                                icon = { 
                                    BadgedBox(
                                        badge = {
                                            if (isTeamItem && pendingTeamRequests > 0) {
                                                Badge(
                                                    containerColor = ProfessionalError, 
                                                    contentColor = Color.White,
                                                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                                ) {
                                                    Text(pendingTeamRequests.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            item.icon, 
                                            null, 
                                            modifier = Modifier.size(22.dp),
                                            tint = if (selected) DeepCharcoal else StoneGray.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                label = { 
                                    Text(
                                        item.title, 
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp,
                                            letterSpacing = 0.2.sp,
                                            fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = if (selected) DeepCharcoal else StoneGray
                                    ) 
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepCharcoal,
                                    unselectedIconColor = StoneGray,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AdminNavGraph(navController = navController)
        }
    }
}
