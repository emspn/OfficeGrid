package com.app.officegrid.tasks.presentation.create_task

data class CreateTaskUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)