package com.esteban.ruano.timers_domain.use_case

import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.timers_domain.repository.TimersRepository

class CreateTimer(
    private val repository: TimersRepository
) {
    suspend operator fun invoke(
        listId: String,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        sendNotificationOnComplete: Boolean,
        order: Int
    ): Result<TimerList> {
        return repository.createTimer(
            listId, name, duration, enabled,
            countsAsPomodoro, sendNotificationOnComplete, order
        )
    }
}

