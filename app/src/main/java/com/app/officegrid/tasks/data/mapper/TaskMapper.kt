package com.app.officegrid.tasks.data.mapper

import com.app.officegrid.tasks.data.local.TaskEntity
import com.app.officegrid.tasks.domain.model.Task

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