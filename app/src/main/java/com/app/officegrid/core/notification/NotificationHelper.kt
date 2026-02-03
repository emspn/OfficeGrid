package com.app.officegrid.core.notification

import com.app.officegrid.core.common.NotificationType
import com.app.officegrid.core.common.domain.repository.NotificationRepository
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to send notifications for various app events.
 * Handles all notification logic and ensures proper routing to recipients.
 */
@Singleton
class NotificationHelper @Inject constructor(
    private val notificationRepository: NotificationRepository
) {

    /**
     * Send notification to ASSIGNED EMPLOYEE when task is assigned
     */
    suspend fun notifyTaskAssigned(task: Task, assignerName: String) {
        android.util.Log.d("NotificationHelper", "🎯 notifyTaskAssigned: ${task.title} to ${task.assignedTo}")

        notificationRepository.sendNotification(
            recipientId = task.assignedTo,
            title = "🎯 New Task Assigned",
            message = "You've been assigned \"${task.title}\" by $assignerName",
            type = NotificationType.TASK_ASSIGNED,
            relatedId = task.id
        ).onSuccess {
            android.util.Log.d("NotificationHelper", "✅ Task assigned notification sent")
        }.onFailure { e ->
            android.util.Log.e("NotificationHelper", "❌ Failed: ${e.message}")
        }
    }

    /**
     * Send notification to ASSIGNED EMPLOYEE when admin updates task
     */
    suspend fun notifyTaskUpdated(task: Task) {
        android.util.Log.d("NotificationHelper", "📝 notifyTaskUpdated: ${task.title}")

        notificationRepository.sendNotification(
            recipientId = task.assignedTo,
            title = "📝 Task Updated",
            message = "\"${task.title}\" has been modified. Review the changes.",
            type = NotificationType.TASK_UPDATED,
            relatedId = task.id
        )
    }

    /**
     * Send notification to ADMIN (task creator) when employee completes task
     */
    suspend fun notifyTaskCompleted(task: Task) {
        android.util.Log.d("NotificationHelper", "✅ notifyTaskCompleted: ${task.title}")

        notificationRepository.sendNotification(
            recipientId = task.createdBy,
            title = "✅ Task Completed",
            message = "\"${task.title}\" has been completed successfully!",
            type = NotificationType.TASK_COMPLETED,
            relatedId = task.id
        )
    }

    /**
     * Send notification when a new remark is added to a task
     */
    suspend fun notifyNewRemark(task: Task, remarkAuthor: String, recipientId: String) {
        android.util.Log.d("NotificationHelper", "💬 notifyNewRemark on ${task.title}")

        notificationRepository.sendNotification(
            recipientId = recipientId,
            title = "💬 New Comment",
            message = "$remarkAuthor commented on \"${task.title}\"",
            type = NotificationType.NEW_REMARK,
            relatedId = task.id
        )
    }

    /**
     * Send notification when a task is overdue
     */
    suspend fun notifyTaskOverdue(task: Task) {
        android.util.Log.d("NotificationHelper", "⏰ notifyTaskOverdue: ${task.title}")

        notificationRepository.sendNotification(
            recipientId = task.assignedTo,
            title = "⏰ Task Overdue",
            message = "\"${task.title}\" is past its due date!",
            type = NotificationType.TASK_OVERDUE,
            relatedId = task.id
        )
    }

    /**
     * Send notification to ADMIN when employee changes task status
     */
    suspend fun notifyStatusChange(task: Task, newStatus: TaskStatus) {
        val (emoji, statusText) = when (newStatus) {
            TaskStatus.TODO -> "📋" to "moved to pending"
            TaskStatus.IN_PROGRESS -> "🔄" to "started working on"
            TaskStatus.PENDING_COMPLETION -> "⏳" to "submitted for review"
            TaskStatus.DONE -> "✅" to "completed"
        }

        android.util.Log.d("NotificationHelper", "$emoji notifyStatusChange: ${task.title} → $newStatus")

        notificationRepository.sendNotification(
            recipientId = task.createdBy,
            title = "$emoji Task $statusText",
            message = "\"${task.title}\" status updated to ${newStatus.name.replace("_", " ")}",
            type = if (newStatus == TaskStatus.DONE) NotificationType.TASK_COMPLETED else NotificationType.TASK_UPDATED,
            relatedId = task.id
        )
    }

    /**
     * Send notification to ADMIN when employee requests to join workspace
     */
    suspend fun notifyJoinRequest(adminId: String, employeeName: String, employeeEmail: String) {
        android.util.Log.d("NotificationHelper", "👤 notifyJoinRequest: $employeeName → Admin $adminId")

        notificationRepository.sendNotification(
            recipientId = adminId,
            title = "👤 New Join Request",
            message = "$employeeName ($employeeEmail) wants to join your workspace",
            type = NotificationType.JOIN_REQUEST
        ).onSuccess {
            android.util.Log.d("NotificationHelper", "✅ Join request notification sent")
        }.onFailure { e ->
            android.util.Log.e("NotificationHelper", "❌ Failed: ${e.message}")
        }
    }

    /**
     * Send notification to EMPLOYEE when admin approves join request
     */
    suspend fun notifyJoinApproved(employeeId: String, workspaceName: String) {
        android.util.Log.d("NotificationHelper", "✅ notifyJoinApproved: $employeeId → $workspaceName")

        notificationRepository.sendNotification(
            recipientId = employeeId,
            title = "✅ Access Granted",
            message = "Welcome! You now have access to \"$workspaceName\"",
            type = NotificationType.JOIN_APPROVED
        )
    }

    /**
     * Send notification to EMPLOYEE when admin rejects join request
     */
    suspend fun notifyJoinRejected(employeeId: String, workspaceName: String) {
        android.util.Log.d("NotificationHelper", "❌ notifyJoinRejected: $employeeId → $workspaceName")

        notificationRepository.sendNotification(
            recipientId = employeeId,
            title = "❌ Access Denied",
            message = "Your request to join \"$workspaceName\" was not approved",
            type = NotificationType.JOIN_REJECTED
        )
    }

    /**
     * Send a system notification
     */
    suspend fun notifySystem(recipientId: String, title: String, message: String) {
        notificationRepository.sendNotification(
            recipientId = recipientId,
            title = title,
            message = message,
            type = NotificationType.SYSTEM
        )
    }
}
