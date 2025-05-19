package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class Timer(
    val id: String,
    val name: String,
    val duration: Long,
    val state: String,
    val enabled: Boolean,
    val remainingSeconds: Long = 0,
    val countsAsPomodoro: Boolean,
    val order: Int
)