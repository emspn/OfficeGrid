package com.app.officegrid.core.ui

sealed interface UiEvent {
    data class ShowMessage(val message: String) : UiEvent
    object SessionExpired : UiEvent
    data class Navigate(val route: String) : UiEvent
}
