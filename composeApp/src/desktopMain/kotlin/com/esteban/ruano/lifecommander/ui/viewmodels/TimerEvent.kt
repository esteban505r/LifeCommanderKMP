package ui.viewmodels

import models.TimerModel

sealed class TimerEvent {
    data class TimerStarted(val timer: TimerModel) : TimerEvent()
    data class TimerFinished(val timer: TimerModel) : TimerEvent()
}