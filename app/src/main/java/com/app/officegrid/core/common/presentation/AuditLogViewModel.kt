package com.app.officegrid.core.common.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.core.common.domain.model.AuditEventType
import com.app.officegrid.core.common.domain.repository.AuditLogRepository
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.asUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditLogViewModel @Inject constructor(
    private val repository: AuditLogRepository
) : ViewModel() {

    private val _selectedType = MutableStateFlow<AuditEventType?>(null)
    val selectedType: StateFlow<AuditEventType?> = _selectedType.asStateFlow()

    val state: StateFlow<UiState<List<com.app.officegrid.core.common.domain.model.AuditLog>>> = combine(
        repository.getAuditLogs(),
        _selectedType
    ) { logs, type ->
        if (type == null) logs
        else logs.filter { it.eventType == type }
    }.asUiState()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    init {
        syncLogs()
    }

    fun onTypeFilterSelected(type: AuditEventType?) {
        _selectedType.value = type
    }

    fun syncLogs() {
        viewModelScope.launch {
            repository.syncAuditLogs()
        }
    }
}
