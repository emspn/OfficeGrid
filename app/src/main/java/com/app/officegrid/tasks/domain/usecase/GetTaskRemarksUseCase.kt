package com.app.officegrid.tasks.domain.usecase

import com.app.officegrid.tasks.domain.model.TaskRemark
import com.app.officegrid.tasks.domain.repository.TaskRemarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTaskRemarksUseCase @Inject constructor(
    private val repository: TaskRemarkRepository
) {
    operator fun invoke(taskId: String): Flow<List<TaskRemark>> {
        return repository.getTaskRemarks(taskId)
    }
}