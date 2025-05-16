package com.esteban.ruano.utils

import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object TimeFormatUtils {
    fun getDayLetter(dateTime: LocalDateTime?): String {
        return when (dateTime?.dayOfWeek) {
            DayOfWeek.MONDAY -> "M"
            DayOfWeek.TUESDAY -> "T"
            DayOfWeek.WEDNESDAY -> "W"
            DayOfWeek.THURSDAY -> "Th"
            DayOfWeek.FRIDAY -> "F"
            DayOfWeek.SATURDAY -> "Sa"
            DayOfWeek.SUNDAY -> "Su"
            else -> "?"
        }
    }

    fun formatDuration(duration: Duration?): String {
        if (duration == null) return "0m"
        return when {
            duration.inWholeHours > 0 -> "${duration.inWholeHours}h ${duration.inWholeMinutes.rem(60)}m"
            else -> "${duration.inWholeMinutes}m"
        }
    }

    fun calculateTimeDifference(time1: LocalTime, time2: LocalTime): Duration {
        val diff = time1.toSecondOfDay() - time2.toSecondOfDay()
        val seconds = if (diff < 0) diff + 24 * 60 * 60 else diff
        return seconds.seconds
    }

    fun isTimeBefore(time1: LocalTime, time2: LocalTime): Boolean {
        return time1.toSecondOfDay() < time2.toSecondOfDay()
    }

    fun isTimeAfter(time1: LocalTime, time2: LocalTime): Boolean {
        return time1.toSecondOfDay() > time2.toSecondOfDay()
    }

    fun isTimeEqual(time1: LocalTime, time2: LocalTime): Boolean {
        return time1.toSecondOfDay() == time2.toSecondOfDay()
    }
} 