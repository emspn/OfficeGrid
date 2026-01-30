package com.app.officegrid.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeMainScreen(
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val unreadNotifications by notificationViewModel.unreadCount.collectAsState()

    val items = listOf(
        NavigationItem("ASSIGNMENTS", Screen.EmployeeTasks.route, Icons.AutoMirrored.Filled.Assignment),
        NavigationItem("DASHBOARD", Screen.AdminDashboard.route, Icons.Default.GridView),
        NavigationItem("SYSTEM_PROFILE", Screen.EmployeeProfile.route, Icons.Default.AccountCircle)
    )

    val currentRoute = currentDestination?.route
    val isDetailScreen = currentRoute in listOf(
        Screen.TaskDetail.route,
        Screen.Notifications.route
    )

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            if (!isDetailScreen) {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "OFFICE_GRID // NODE", 
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = DeepCharcoal
                        ) 
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
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
                                Icon(Icons.Default.Notifications, null, tint = DeepCharcoal, modifier = Modifier.size(20.dp))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmBackground),
                    windowInsets = WindowInsets.statusBars
                )
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    item.icon, 
                                    null, 
                                    modifier = Modifier.size(22.dp),
                                    tint = if (selected) DeepCharcoal else StoneGray
                                ) 
                            },
                            label = { 
                                Text(
                                    item.title, 
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp, 
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
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
                                indicatorColor = Color.Transparent,
                                selectedIconColor = DeepCharcoal,
                                unselectedIconColor = StoneGray,
                                selectedTextColor = DeepCharcoal,
                                unselectedTextColor = StoneGray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            EmployeeNavGraph(navController = navController)
        }
    }
}
