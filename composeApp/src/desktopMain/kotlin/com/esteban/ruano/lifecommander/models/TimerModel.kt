package models

import java.util.UUID

data class TimerModel(
    val id : String = UUID.randomUUID().toString(),
    val name: String,
    val timeRemaining: Long,
    val startValue: Long,
    val showDialog: Boolean = false,
    val endValue: Long,
    val step: Long,
    val isPomodoro: Boolean = false,
    val pomodoroCount: Int = 0
)