package com.esteban.ruano.timers_domain.use_case

import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.timers_domain.repository.TimersRepository

class GetTimerLists(
    private val repository: TimersRepository
) {
    suspend operator fun invoke(): Result<List<TimerList>> {
        return repository.getTimerLists()
    }
}

