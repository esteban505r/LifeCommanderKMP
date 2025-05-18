package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class TimerList(
    val id: String,
    val name : String,
    val timers : List<Timer> = emptyList(),
    val loopTimers : Boolean,
    val pomodoroGrouped : Boolean,
    val status : String,
)