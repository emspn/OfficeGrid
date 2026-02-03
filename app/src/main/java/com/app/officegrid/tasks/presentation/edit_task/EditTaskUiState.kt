package com.app.officegrid.tasks.presentation.edit_task

data class EditTaskUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
