package com.app.officegrid.tasks.presentation.task_list

import com.app.officegrid.tasks.domain.model.Task

data class TaskListUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val error: String? = null
)