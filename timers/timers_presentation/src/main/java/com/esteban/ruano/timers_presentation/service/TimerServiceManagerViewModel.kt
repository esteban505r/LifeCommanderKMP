package com.esteban.ruano.timers_presentation.service

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimerServiceManagerViewModel @Inject constructor(
    val timerServiceManager: TimerServiceManager
) : ViewModel() {
    init {
        timerServiceManager.initialize()
    }
}

