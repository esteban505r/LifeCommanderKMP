package com.esteban.ruano.core.utils

sealed class UiEvent {
    data object Success: UiEvent()
    data object NavigateUp: UiEvent()

}