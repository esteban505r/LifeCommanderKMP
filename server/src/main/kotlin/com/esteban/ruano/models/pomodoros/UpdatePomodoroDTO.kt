package com.esteban.ruano.models.pomodoros

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePomodoroDTO(
    val startDateTime: String? = null,
    val endDateTime: String? = null,
    val updatedAt: String? = null
) 