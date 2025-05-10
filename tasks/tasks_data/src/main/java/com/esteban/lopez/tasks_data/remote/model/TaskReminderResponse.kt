package com.esteban.ruano.tasks_data.remote.model

import com.esteban.ruano.core.models.ReminderResponseInterface
import kotlinx.serialization.Serializable

@Serializable
data class TaskReminderResponse(
    override val id: String? = null,
    override val time: Long,
): ReminderResponseInterface