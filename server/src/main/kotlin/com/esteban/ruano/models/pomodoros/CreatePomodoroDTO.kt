package com.esteban.ruano.models.pomodoros

import kotlinx.serialization.Serializable

@Serializable
data class CreatePomodoroDTO(
    val startDateTime: String,
    val endDateTime: String,
    val createdAt: String? = null
) 