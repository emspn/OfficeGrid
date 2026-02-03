package com.app.officegrid.tasks.domain.usecase

import com.app.officegrid.tasks.domain.model.Task
import com.app.officegrid.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ✨ GET ALL TASKS USECASE
 * Retrieves all operational units for the active node context.
 */
class GetAllTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return repository.getAllTasks()
    }
}
