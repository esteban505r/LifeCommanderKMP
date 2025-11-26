package com.esteban.ruano.timers_domain.use_case

import com.esteban.ruano.timers_domain.repository.TimersRepository

class PauseTimer(
    private val repository: TimersRepository
) {
    suspend operator fun invoke(listId: String, timerId: String?): Result<Unit> {
        return repository.pauseTimer(listId, timerId)
    }
}

