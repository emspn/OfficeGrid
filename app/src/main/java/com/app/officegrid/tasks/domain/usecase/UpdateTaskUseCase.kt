package com.app.officegrid.tasks.domain.usecase

import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        return repository.updateTask(task)
    }
}
