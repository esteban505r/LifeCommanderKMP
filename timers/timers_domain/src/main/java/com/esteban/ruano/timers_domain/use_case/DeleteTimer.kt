package com.esteban.ruano.timers_domain.use_case

import com.esteban.ruano.timers_domain.repository.TimersRepository

class DeleteTimer(
    private val repository: TimersRepository
) {
    suspend operator fun invoke(timerId: String): Result<Unit> {
        return repository.deleteTimer(timerId)
    }
}

