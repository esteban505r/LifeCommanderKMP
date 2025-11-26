package com.esteban.ruano.timers_domain.use_case

import com.esteban.ruano.timers_domain.repository.TimersRepository

class ResumeTimer(
    private val repository: TimersRepository
) {
    suspend operator fun invoke(listId: String): Result<Unit> {
        return repository.resumeTimer(listId)
    }
}

