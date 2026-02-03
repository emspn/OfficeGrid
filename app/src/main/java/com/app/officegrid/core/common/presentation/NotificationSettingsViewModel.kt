package com.app.officegrid.core.common.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.core.common.domain.model.NotificationSettings
import com.app.officegrid.core.common.domain.repository.NotificationSettingsRepository
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.asUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val repository: NotificationSettingsRepository
) : ViewModel() {

    val state: StateFlow<UiState<NotificationSettings?>> = repository.getSettings()
        .asUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun updateSettings(settings: NotificationSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }
}
