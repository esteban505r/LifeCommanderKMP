package com.esteban.ruano.timers_presentation.ui.intent

import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.TimerList

sealed class TimerIntent : UserIntent {
    data object FetchTimerLists : TimerIntent()
    data class FetchTimerList(val listId: String) : TimerIntent()
    data class CreateTimerList(
        val name: String,
        val loopTimers: Boolean,
        val pomodoroGrouped: Boolean
    ) : TimerIntent()
    data class UpdateTimerList(
        val listId: String,
        val name: String,
        val loopTimers: Boolean,
        val pomodoroGrouped: Boolean
    ) : TimerIntent()
    data class DeleteTimerList(val listId: String) : TimerIntent()
    data class CreateTimer(
        val listId: String,
        val name: String,
        val duration: Long,
        val enabled: Boolean,
        val countsAsPomodoro: Boolean,
        val sendNotificationOnComplete: Boolean,
        val order: Int
    ) : TimerIntent()
    data class UpdateTimer(
        val timerId: String,
        val name: String,
        val duration: Long,
        val enabled: Boolean,
        val countsAsPomodoro: Boolean,
        val sendNotificationOnComplete: Boolean,
        val order: Int
    ) : TimerIntent()
    data class DeleteTimer(val timerId: String) : TimerIntent()
    data class StartTimer(val timerList: TimerList) : TimerIntent()
    data object PauseTimer : TimerIntent()
    data object ResumeTimer : TimerIntent()
    data object StopTimer : TimerIntent()
    data object ConnectWebSocket : TimerIntent()
    data object ReconnectWebSocket : TimerIntent()
}

sealed class TimerEffect : Effect {
    data object NavigateUp : TimerEffect()
    data class NavigateToTimerListDetail(val listId: String) : TimerEffect()
    data class ShowSnackBar(val message: String, val type: SnackbarType) : TimerEffect()
}


