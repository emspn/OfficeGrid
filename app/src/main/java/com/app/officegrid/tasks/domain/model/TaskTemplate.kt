package com.app.officegrid.tasks.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: TemplateCategory,
    val tasks: List<TemplateTask>,
    val estimatedDuration: String, // "2 weeks", "3 days"
    val popularity: Int = 0
)

@Serializable
data class TemplateTask(
    val title: String,
    val description: String,
    val priority: TaskPriority,
    val estimatedDays: Int,
    val dependsOn: Int? = null, // Index of task it depends on
    val assigneeRole: String? = null // "Manager", "Developer", "Designer"
)

enum class TemplateCategory {
    CLIENT_ONBOARDING,
    EMPLOYEE_ONBOARDING,
    BUG_FIX_WORKFLOW,
    SPRINT_PLANNING,
    SALES_PIPELINE,
    MARKETING_CAMPAIGN,
    MONTHLY_REPORTING,
    PROJECT_KICKOFF,
    CUSTOM
}

// Pre-built templates
object TaskTemplates {

    val CLIENT_ONBOARDING = TaskTemplate(
        id = "template_client_onboarding",
        name = "Client Onboarding",
        description = "Complete workflow for onboarding new clients",
        category = TemplateCategory.CLIENT_ONBOARDING,
        estimatedDuration = "2 weeks",
        popularity = 95,
        tasks = listOf(
            TemplateTask(
                title = "Initial Client Meeting",
                description = "Discuss requirements and expectations",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                assigneeRole = "Account Manager"
            ),
            TemplateTask(
                title = "Sign Contract & NDA",
                description = "Get legal documents signed",
                priority = TaskPriority.HIGH,
                estimatedDays = 2,
                dependsOn = 0,
                assigneeRole = "Legal Team"
            ),
            TemplateTask(
                title = "Setup Client Account",
                description = "Create accounts in all systems",
                priority = TaskPriority.MEDIUM,
                estimatedDays = 1,
                dependsOn = 1,
                assigneeRole = "Admin"
            ),
            TemplateTask(
                title = "Kickoff Meeting",
                description = "Introduce team and timeline",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                dependsOn = 2,
                assigneeRole = "Project Manager"
            ),
            TemplateTask(
                title = "Requirements Gathering",
                description = "Document all client requirements",
                priority = TaskPriority.HIGH,
                estimatedDays = 3,
                dependsOn = 3,
                assigneeRole = "Business Analyst"
            )
        )
    )

    val EMPLOYEE_ONBOARDING = TaskTemplate(
        id = "template_employee_onboarding",
        name = "New Employee Onboarding",
        description = "Complete checklist for new hires",
        category = TemplateCategory.EMPLOYEE_ONBOARDING,
        estimatedDuration = "1 week",
        popularity = 98,
        tasks = listOf(
            TemplateTask(
                title = "Send Welcome Email",
                description = "Welcome and first day instructions",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                assigneeRole = "HR"
            ),
            TemplateTask(
                title = "Prepare Workstation",
                description = "Setup desk, computer, accounts",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                assigneeRole = "IT Team"
            ),
            TemplateTask(
                title = "First Day Orientation",
                description = "Company tour and introductions",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                dependsOn = 1,
                assigneeRole = "HR Manager"
            ),
            TemplateTask(
                title = "Assign Buddy/Mentor",
                description = "Pair with experienced team member",
                priority = TaskPriority.MEDIUM,
                estimatedDays = 1,
                dependsOn = 2,
                assigneeRole = "Team Lead"
            ),
            TemplateTask(
                title = "Complete Documentation",
                description = "ID proof, bank details, tax forms",
                priority = TaskPriority.HIGH,
                estimatedDays = 2,
                assigneeRole = "HR"
            ),
            TemplateTask(
                title = "Training Sessions",
                description = "Product and process training",
                priority = TaskPriority.MEDIUM,
                estimatedDays = 3,
                dependsOn = 3,
                assigneeRole = "Training Team"
            )
        )
    )

    val BUG_FIX_WORKFLOW = TaskTemplate(
        id = "template_bug_fix",
        name = "Bug Fix Workflow",
        description = "Standard process for fixing bugs",
        category = TemplateCategory.BUG_FIX_WORKFLOW,
        estimatedDuration = "3 days",
        popularity = 92,
        tasks = listOf(
            TemplateTask(
                title = "Reproduce Bug",
                description = "Verify and document reproduction steps",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                assigneeRole = "QA Engineer"
            ),
            TemplateTask(
                title = "Analyze Root Cause",
                description = "Identify the cause of the bug",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                dependsOn = 0,
                assigneeRole = "Developer"
            ),
            TemplateTask(
                title = "Implement Fix",
                description = "Code the solution",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                dependsOn = 1,
                assigneeRole = "Developer"
            ),
            TemplateTask(
                title = "Code Review",
                description = "Peer review of the fix",
                priority = TaskPriority.MEDIUM,
                estimatedDays = 1,
                dependsOn = 2,
                assigneeRole = "Senior Developer"
            ),
            TemplateTask(
                title = "QA Testing",
                description = "Verify fix works and no regression",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                dependsOn = 3,
                assigneeRole = "QA Engineer"
            ),
            TemplateTask(
                title = "Deploy to Production",
                description = "Release the fix",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                dependsOn = 4,
                assigneeRole = "DevOps"
            )
        )
    )

    val SPRINT_PLANNING = TaskTemplate(
        id = "template_sprint_planning",
        name = "Sprint Planning",
        description = "2-week sprint setup checklist",
        category = TemplateCategory.SPRINT_PLANNING,
        estimatedDuration = "2 weeks",
        popularity = 88,
        tasks = listOf(
            TemplateTask(
                title = "Review Previous Sprint",
                description = "Retrospective and learnings",
                priority = TaskPriority.MEDIUM,
                estimatedDays = 1,
                assigneeRole = "Scrum Master"
            ),
            TemplateTask(
                title = "Backlog Refinement",
                description = "Prioritize and estimate stories",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                dependsOn = 0,
                assigneeRole = "Product Owner"
            ),
            TemplateTask(
                title = "Sprint Planning Meeting",
                description = "Select stories for sprint",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                dependsOn = 1,
                assigneeRole = "Scrum Master"
            ),
            TemplateTask(
                title = "Break Down Tasks",
                description = "Create subtasks from stories",
                priority = TaskPriority.MEDIUM,
                estimatedDays = 1,
                dependsOn = 2,
                assigneeRole = "Development Team"
            ),
            TemplateTask(
                title = "Set Sprint Goals",
                description = "Define what success looks like",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                dependsOn = 3,
                assigneeRole = "Product Owner"
            )
        )
    )

    val MARKETING_CAMPAIGN = TaskTemplate(
        id = "template_marketing_campaign",
        name = "Marketing Campaign Launch",
        description = "Launch new marketing campaign",
        category = TemplateCategory.MARKETING_CAMPAIGN,
        estimatedDuration = "3 weeks",
        popularity = 85,
        tasks = listOf(
            TemplateTask(
                title = "Campaign Strategy",
                description = "Define goals, audience, channels",
                priority = TaskPriority.HIGH,
                estimatedDays = 2,
                assigneeRole = "Marketing Manager"
            ),
            TemplateTask(
                title = "Create Content",
                description = "Write copy, design graphics",
                priority = TaskPriority.HIGH,
                estimatedDays = 5,
                dependsOn = 0,
                assigneeRole = "Content Team"
            ),
            TemplateTask(
                title = "Setup Landing Pages",
                description = "Create and test landing pages",
                priority = TaskPriority.MEDIUM,
                estimatedDays = 3,
                dependsOn = 1,
                assigneeRole = "Web Developer"
            ),
            TemplateTask(
                title = "Configure Analytics",
                description = "Setup tracking and goals",
                priority = TaskPriority.MEDIUM,
                estimatedDays = 1,
                dependsOn = 2,
                assigneeRole = "Analytics Team"
            ),
            TemplateTask(
                title = "Launch Campaign",
                description = "Go live across all channels",
                priority = TaskPriority.HIGH,
                estimatedDays = 1,
                dependsOn = 3,
                assigneeRole = "Marketing Manager"
            ),
            TemplateTask(
                title = "Monitor & Optimize",
                description = "Track performance and adjust",
                priority = TaskPriority.MEDIUM,
                estimatedDays = 7,
                dependsOn = 4,
                assigneeRole = "Marketing Analyst"
            )
        )
    )

    // List of all templates
    val ALL_TEMPLATES = listOf(
        CLIENT_ONBOARDING,
        EMPLOYEE_ONBOARDING,
        BUG_FIX_WORKFLOW,
        SPRINT_PLANNING,
        MARKETING_CAMPAIGN
    )

    // Get templates by category
    fun getByCategory(category: TemplateCategory): List<TaskTemplate> {
        return ALL_TEMPLATES.filter { it.category == category }
    }

    // Get popular templates
    fun getPopular(limit: Int = 5): List<TaskTemplate> {
        return ALL_TEMPLATES.sortedByDescending { it.popularity }.take(limit)
    }
}

