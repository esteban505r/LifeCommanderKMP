package com.esteban.ruano.timers_domain.use_case

import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.timers_domain.repository.TimersRepository

class GetTimerList(
    private val repository: TimersRepository
) {
    suspend operator fun invoke(listId: String): Result<TimerList> {
        return repository.getTimerList(listId)
    }
}

