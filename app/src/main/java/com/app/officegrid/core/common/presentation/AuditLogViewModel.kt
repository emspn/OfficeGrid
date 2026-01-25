package com.app.officegrid.core.common.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.core.common.domain.repository.AuditLogRepository
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.asUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditLogViewModel @Inject constructor(
    private val repository: AuditLogRepository
) : ViewModel() {

    val state: StateFlow<UiState<List<com.app.officegrid.core.common.domain.model.AuditLog>>> = repository.getAuditLogs()
        .asUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    init {
        syncLogs()
    }

    fun syncLogs() {
        viewModelScope.launch {
            repository.syncAuditLogs()
        }
    }
}