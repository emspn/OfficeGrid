package com.app.officegrid.tasks.domain.usecase

import com.app.officegrid.tasks.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<Unit> {
        return repository.deleteTask(taskId)
    }
}
