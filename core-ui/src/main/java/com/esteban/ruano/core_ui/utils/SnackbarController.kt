package com.esteban.ruano.core_ui.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class SnackbarAction(
    val name: String,
    val action: () -> Unit
)

data class SnackbarEvent(
    val customSnackBarVisuals: CustomSnackbarVisualsWithUiText,
    val action: SnackbarAction? = null
)

object SnackbarController {


    private val _events = Channel<SnackbarEvent>()
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(event: SnackbarEvent) {
        _events.send(event)
    }



}