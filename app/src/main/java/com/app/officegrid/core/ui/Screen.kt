package com.app.officegrid.core.ui

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object SignupRole : Screen("signup_role")
    object CompanySignup : Screen("company_signup")
    object EmployeeSignup : Screen("employee_signup")
    
    // Admin (Organisation) specific
    object AdminDashboard : Screen("admin_dashboard")
    object AdminTasks : Screen("admin_tasks")
    object AdminCreateTask : Screen("admin_create_task")
    object AdminEditTask : Screen("admin_edit_task/{taskId}") {
        fun createRoute(taskId: String) = "admin_edit_task/$taskId"
    }
    // 🚀 NEW: Task Creation Success Screen
    object AdminTaskSuccess : Screen("admin_task_success/{title}/{employee}/{date}") {
        fun createRoute(title: String, employee: String, date: Long) = 
            "admin_task_success/$title/$employee/$date"
    }

    object AdminTeam : Screen("admin_team")
    object AdminProfile : Screen("admin_profile")
    object AdminAuditLogs : Screen("admin_audit_logs")
    object AdminAnalytics : Screen("admin_analytics")
    object AdminSettings : Screen("admin_settings")
    object OrganizationSettings : Screen("organization_settings")

    // Employee specific
    object EmployeeTasks : Screen("employee_tasks")
    object EmployeeProfile : Screen("employee_profile")
    
    // Shared
    object Notifications : Screen("notifications")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
}
