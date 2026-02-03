package com.app.officegrid.core.common.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.officegrid.core.common.AppNotification
import com.app.officegrid.core.common.domain.repository.NotificationRepository
import com.app.officegrid.core.ui.UiState
import com.app.officegrid.core.ui.asUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    val state: StateFlow<UiState<List<AppNotification>>> = try {
        repository.getNotifications()
            .asUiState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )
    } catch (e: Exception) {
        android.util.Log.e("NotificationViewModel", "Failed to initialize notifications: ${e.message}", e)
        kotlinx.coroutines.flow.MutableStateFlow(UiState.Error(e.message ?: "Failed to load notifications"))
    }

    val unreadCount: StateFlow<Int> = try {
        repository.getUnreadCount()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )
    } catch (e: Exception) {
        android.util.Log.e("NotificationViewModel", "Failed to get unread count: ${e.message}", e)
        kotlinx.coroutines.flow.MutableStateFlow(0)
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }
}
