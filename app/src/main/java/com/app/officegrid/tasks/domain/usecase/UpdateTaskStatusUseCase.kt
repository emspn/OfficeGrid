package com.app.officegrid.tasks.domain.usecase

import com.app.officegrid.tasks.domain.model.TaskStatus
import com.app.officegrid.tasks.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskStatusUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, status: TaskStatus): Result<Unit> {
        return repository.updateTaskStatus(taskId, status)
    }
}
