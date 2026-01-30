package com.app.officegrid.tasks.data.mapper

import com.app.officegrid.tasks.data.local.TaskEntity
import com.app.officegrid.tasks.data.remote.dto.TaskDto
import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.model.TaskPriority
import com.app.officegrid.tasks.domain.model.TaskStatus

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
        dueDate = dueDate
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
        dueDate = dueDate
    )
}

fun TaskDto.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        status = TaskStatus.valueOf(status),
        priority = TaskPriority.valueOf(priority),
        assignedTo = assigned_to,
        createdBy = created_by,
        companyId = company_id,
        dueDate = due_date
    )
}

fun TaskDto.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        status = TaskStatus.valueOf(status),
        priority = TaskPriority.valueOf(priority),
        assignedTo = assigned_to,
        createdBy = created_by,
        companyId = company_id,
        dueDate = due_date
    )
}

fun Task.toDto(companyId: String): TaskDto {
    return TaskDto(
        id = id,
        title = title,
        description = description,
        status = status.name,
        priority = priority.name,
        assigned_to = assignedTo,
        created_by = createdBy,
        company_id = companyId,
        due_date = dueDate
    )
}
