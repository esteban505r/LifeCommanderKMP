package com.esteban.ruano.timers_domain.use_case

import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.timers_domain.repository.TimersRepository

class UpdateTimer(
    private val repository: TimersRepository
) {
    suspend operator fun invoke(
        timerId: String,
        name: String? = null,
        timerListId: String? = null,
        duration: Long? = null,
        enabled: Boolean? = null,
        countsAsPomodoro: Boolean? = null,
        sendNotificationOnComplete: Boolean? = null,
        order: Int? = null
    ): Result<TimerList> {
        return repository.updateTimer(
            timerId, name, timerListId, duration, enabled,
            countsAsPomodoro, sendNotificationOnComplete, order
        )
    }
}

