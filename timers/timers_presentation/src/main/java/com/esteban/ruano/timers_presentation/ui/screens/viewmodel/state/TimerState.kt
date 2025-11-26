package com.esteban.ruano.timers_presentation.ui.screens.viewmodel.state

import com.esteban.lopez.core.utils.AppConstants.EMPTY_STRING
import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.timer.TimerConnectionState
import com.esteban.ruano.lifecommander.timer.TimerNotification
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState

data class TimerState(
    val timerLists: List<TimerList> = emptyList(),
    val timerDetailList: TimerList? = null,
    val timerPlaybackState: TimerPlaybackState? = null,
    val connectionState: TimerConnectionState = TimerConnectionState.Disconnected,
    val notifications: List<TimerNotification> = emptyList(),
    val listNotifications: List<TimerNotification> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = EMPTY_STRING,
    val timerDetailLoading: Boolean = false,
    val timerDetailError: String? = null,
) : ViewState


