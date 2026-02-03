package com.app.officegrid.tasks.domain.usecase

import com.app.officegrid.tasks.domain.model.Task
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExportTasksUseCase @Inject constructor() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    operator fun invoke(tasks: List<Task>, format: ExportFormat = ExportFormat.CSV): String {
        return when (format) {
            ExportFormat.CSV -> exportAsCsv(tasks)
            ExportFormat.TEXT -> exportAsText(tasks)
            ExportFormat.MARKDOWN -> exportAsMarkdown(tasks)
        }
    }

    private fun exportAsCsv(tasks: List<Task>): String {
        val builder = StringBuilder()
        builder.appendLine("ID,Title,Description,Status,Priority,Assigned To,Created By,Company,Due Date")

        tasks.forEach { task ->
            val dueDate = dateFormat.format(Date(task.dueDate))
            builder.appendLine(
                "${task.id},\"${task.title}\",\"${task.description}\",${task.status}," +
                "${task.priority},${task.assignedTo},${task.createdBy},${task.companyId},\"$dueDate\""
            )
        }

        return builder.toString()
    }

    private fun exportAsText(tasks: List<Task>): String {
        val builder = StringBuilder()
        builder.appendLine("OFFICEGRID TASK EXPORT")
        builder.appendLine("Generated: ${dateFormat.format(Date())}")
        builder.appendLine("Total Tasks: ${tasks.size}")
        builder.appendLine("=" .repeat(80))
        builder.appendLine()

        tasks.forEachIndexed { index, task ->
            builder.appendLine("TASK #${index + 1}")
            builder.appendLine("-".repeat(80))
            builder.appendLine("Title: ${task.title}")
            builder.appendLine("Description: ${task.description}")
            builder.appendLine("Status: ${task.status}")
            builder.appendLine("Priority: ${task.priority}")
            builder.appendLine("Assigned To: ${task.assignedTo}")
            builder.appendLine("Created By: ${task.createdBy}")
            builder.appendLine("Due Date: ${dateFormat.format(Date(task.dueDate))}")
            builder.appendLine()
        }

        return builder.toString()
    }

    private fun exportAsMarkdown(tasks: List<Task>): String {
        val builder = StringBuilder()
        builder.appendLine("# OfficeGrid Task Export")
        builder.appendLine()
        builder.appendLine("**Generated:** ${dateFormat.format(Date())}")
        builder.appendLine("**Total Tasks:** ${tasks.size}")
        builder.appendLine()
        builder.appendLine("---")
        builder.appendLine()

        // Group by status
        val grouped = tasks.groupBy { it.status }

        grouped.forEach { (status, tasksInStatus) ->
            builder.appendLine("## ${status.name.replace('_', ' ')} (${tasksInStatus.size})")
            builder.appendLine()

            tasksInStatus.forEach { task ->
                val emoji = when (task.priority) {
                    com.app.officegrid.tasks.domain.model.TaskPriority.HIGH -> "🔴"
                    com.app.officegrid.tasks.domain.model.TaskPriority.MEDIUM -> "🟡"
                    com.app.officegrid.tasks.domain.model.TaskPriority.LOW -> "🟢"
                }

                builder.appendLine("### $emoji ${task.title}")
                builder.appendLine()
                builder.appendLine("**Description:** ${task.description}")
                builder.appendLine()
                builder.appendLine("- **Priority:** ${task.priority}")
                builder.appendLine("- **Assigned To:** ${task.assignedTo}")
                builder.appendLine("- **Due Date:** ${dateFormat.format(Date(task.dueDate))}")
                builder.appendLine()
            }
        }

        return builder.toString()
    }
}

enum class ExportFormat {
    CSV, TEXT, MARKDOWN
}

