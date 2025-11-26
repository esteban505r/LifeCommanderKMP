package com.esteban.ruano.timers_domain.use_case

import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.timers_domain.repository.TimersRepository

class UpdateTimerList(
    private val repository: TimersRepository
) {
    suspend operator fun invoke(
        listId: String,
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ): Result<TimerList> {
        return repository.updateTimerList(listId, name, loopTimers, pomodoroGrouped)
    }
}

