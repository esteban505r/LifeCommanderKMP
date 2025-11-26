package com.esteban.ruano.timers_domain.use_case

import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.timers_domain.repository.TimersRepository

class CreateTimerList(
    private val repository: TimersRepository
) {
    suspend operator fun invoke(
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ): Result<TimerList> {
        return repository.createTimerList(name, loopTimers, pomodoroGrouped)
    }
}

