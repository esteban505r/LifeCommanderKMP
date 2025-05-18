package com.esteban.ruano.lifecommander.models

data class Timer(
    val id: String,
    val name: String,
    val duration: Int,
    val enabled: Boolean,
    val countsAsPomodoro: Boolean,
    val order: Int
)