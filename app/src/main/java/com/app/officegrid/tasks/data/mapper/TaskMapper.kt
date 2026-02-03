package com.app.officegrid.tasks.data.mapper

import com.app.officegrid.tasks.data.local.TaskEntity
import com.app.officegrid.tasks.data.remote.TaskDto
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        status = status,
        priority = priority,
        assignedTo = assignedTo,
        createdBy = createdBy,
        companyId = companyId,
        dueDate = dueDate,
        createdAt = createdAt
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        status = status,
        priority = priority,
        assignedTo = assignedTo,
        createdBy = createdBy,
        companyId = companyId,
        dueDate = dueDate,
        createdAt = createdAt
    )
}

fun TaskDto.toDomain(): Task {
    val createdTime = try {
        if (!created_at.isNullOrEmpty()) {
            OffsetDateTime.parse(created_at).toInstant().toEpochMilli()
        } else 0L
    } catch (e: Exception) {
        0L
    }

    // Parse due_date from ISO string to Long
    val dueDateLong = try {
        if (due_date.isNotEmpty()) {
            OffsetDateTime.parse(due_date).toInstant().toEpochMilli()
        } else System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    return Task(
        id = id ?: "",
        title = title,
        description = description,
        status = try { TaskStatus.valueOf(status) } catch (e: Exception) { TaskStatus.TODO },
        priority = try { TaskPriority.valueOf(priority) } catch (e: Exception) { TaskPriority.MEDIUM },
        assignedTo = assigned_to,
        createdBy = created_by,
        companyId = company_id,
        dueDate = dueDateLong,
        createdAt = createdTime
    )
}

fun TaskDto.toEntity(): TaskEntity {
    val createdTime = try {
        if (!created_at.isNullOrEmpty()) {
            OffsetDateTime.parse(created_at).toInstant().toEpochMilli()
        } else 0L
    } catch (e: Exception) {
        0L
    }

    // Parse due_date from ISO string to Long
    val dueDateLong = try {
        if (due_date.isNotEmpty()) {
            OffsetDateTime.parse(due_date).toInstant().toEpochMilli()
        } else System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    return TaskEntity(
        id = id ?: "",
        title = title,
        description = description,
        status = try { TaskStatus.valueOf(status) } catch (e: Exception) { TaskStatus.TODO },
        priority = try { TaskPriority.valueOf(priority) } catch (e: Exception) { TaskPriority.MEDIUM },
        assignedTo = assigned_to,
        createdBy = created_by,
        companyId = company_id,
        dueDate = dueDateLong,
        createdAt = createdTime
    )
}

fun Task.toDto(companyId: String): TaskDto {
    // Convert Long timestamp to ISO-8601 format for Supabase
    val dueDateStr = java.time.Instant.ofEpochMilli(dueDate)
        .atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    return TaskDto(
        id = id,
        title = title,
        description = description,
        status = status.name,
        priority = priority.name,
        assigned_to = assignedTo,
        created_by = createdBy,
        company_id = companyId,
        due_date = dueDateStr,
        created_at = null // Supabase handles this with DEFAULT now()
    )
}
