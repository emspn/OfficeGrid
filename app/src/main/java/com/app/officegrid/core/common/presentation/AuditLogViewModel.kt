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
import java.util.Calendar
import javax.inject.Inject

enum class DateFilterType {
    ALL, WEEK, MONTH, SIX_MONTHS, YEAR, CUSTOM
}

@HiltViewModel
class AuditLogViewModel @Inject constructor(
    private val repository: AuditLogRepository
) : ViewModel() {

    private val _selectedType = MutableStateFlow<AuditEventType?>(null)
    val selectedType: StateFlow<AuditEventType?> = _selectedType.asStateFlow()

    private val _dateFilter = MutableStateFlow(DateFilterType.ALL)
    val dateFilter: StateFlow<DateFilterType> = _dateFilter.asStateFlow()

    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val customDateRange: StateFlow<Pair<Long, Long>?> = _customDateRange.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val state: StateFlow<UiState<List<com.app.officegrid.core.common.domain.model.AuditLog>>> = combine(
        repository.getAuditLogs(),
        _selectedType,
        _dateFilter,
        _customDateRange,
        _searchQuery
    ) { logs, type, dateFilter, customRange, query ->
        var filtered = logs

        // 1. Type Filter
        if (type != null) {
            filtered = filtered.filter { it.eventType == type }
        }

        // 2. Search Filter
        if (query.isNotBlank()) {
            filtered = filtered.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) ||
                it.userEmail.contains(query, ignoreCase = true)
            }
        }

        // 3. Date Filter
        val now = System.currentTimeMillis()
        filtered = when (dateFilter) {
            DateFilterType.ALL -> filtered
            DateFilterType.WEEK -> filtered.filter { it.createdAt >= now - (7 * 24 * 60 * 60 * 1000L) }
            DateFilterType.MONTH -> filtered.filter { it.createdAt >= now - (30 * 24 * 60 * 60 * 1000L) }
            DateFilterType.SIX_MONTHS -> filtered.filter { it.createdAt >= now - (180 * 24 * 60 * 60 * 1000L) }
            DateFilterType.YEAR -> filtered.filter { it.createdAt >= now - (365 * 24 * 60 * 60 * 1000L) }
            DateFilterType.CUSTOM -> {
                customRange?.let { (start, end) ->
                    filtered.filter { it.createdAt in start..end }
                } ?: filtered
            }
        }

        filtered.sortedByDescending { it.createdAt }
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

    fun onDateFilterSelected(filter: DateFilterType) {
        _dateFilter.value = filter
        if (filter != DateFilterType.CUSTOM) {
            _customDateRange.value = null
        }
    }

    fun onCustomDateRangeSelected(start: Long, end: Long) {
        _customDateRange.value = Pair(start, end)
        _dateFilter.value = DateFilterType.CUSTOM
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun syncLogs() {
        viewModelScope.launch {
            repository.syncAuditLogs()
        }
    }

    fun exportAsPdf() {
        // Implementation for PDF export would go here
        // For now, we'll just show a success message in UI via events if needed
    }
}
