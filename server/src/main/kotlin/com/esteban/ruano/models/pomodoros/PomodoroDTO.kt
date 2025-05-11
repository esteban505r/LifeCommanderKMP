package com.esteban.ruano.models.pomodoros

import kotlinx.serialization.Serializable

@Serializable
data class PomodoroDTO(
    val id: String,
    val startDateTime: String,
    val endDateTime: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
) 