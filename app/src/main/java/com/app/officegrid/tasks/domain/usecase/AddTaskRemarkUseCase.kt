package com.app.officegrid.tasks.domain.usecase

import com.app.officegrid.tasks.domain.repository.TaskRemarkRepository
import javax.inject.Inject

class AddTaskRemarkUseCase @Inject constructor(
    private val repository: TaskRemarkRepository
) {
    suspend operator fun invoke(taskId: String, message: String): Result<Unit> {
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Remark message cannot be empty"))
        }
        return repository.addTaskRemark(taskId, message)
    }
}