package com.esteban.ruano.lifecommander.timer

sealed class TimerConnectionState {
    object Connected : TimerConnectionState()
    object Disconnected : TimerConnectionState()
    object Reconnecting : TimerConnectionState()
    data class Error(val message: String) : TimerConnectionState()
}

