package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class HabitReminder(
    val id: String? = null,
    val time: Long,
)