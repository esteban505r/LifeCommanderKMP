package com.esteban.ruano.timers_domain.use_case

import com.esteban.ruano.timers_domain.repository.TimersRepository

class StartTimer(
    private val repository: TimersRepository
) {
    suspend operator fun invoke(listId: String, timerId: String?): Result<Unit> {
        return repository.startTimer(listId, timerId)
    }
}

