package com.esteban.ruano.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.esteban.ruano.ui.DarkGray
import com.esteban.ruano.ui.LightGray
import com.esteban.ruano.ui.SoftBlue
import com.esteban.ruano.ui.SoftGreen
import com.esteban.ruano.ui.SoftRed
import com.esteban.ruano.ui.SoftYellow
import kotlinx.datetime.*

object DateUIUtils {

    const val DAYS_OF_THE_WEEK = 7

    fun LocalDate.formatDefault(): String {
        val day = dayOfMonth.toString().padStart(2, '0')
        val month = monthNumber.toString().padStart(2, '0')
        return "$day/$month/$year"
    }

    fun compareTimes(t1: String, t2: String): Int {
        val time1 = timeToIntPair(t1)
        val time2 = timeToIntPair(t2)
        return time1.first.compareTo(time2.first).let {
            if (it == 0) time1.second.compareTo(time2.second) else it
        }
    }


    fun LocalDateTime.formatDefault(): String {
        val date = date.formatDefault()
        val time = formatToTimeString()
        return "$date $time"
    }

    fun LocalTime.formatDefault(): String {
        val hourStr = hour.toString().padStart(2, '0')
        val minuteStr = minute.toString().padStart(2, '0')
        return "$hourStr:$minuteStr"
    }

    fun LocalDateTime.formatToTimeString(): String {
        val hourStr = hour.toString().padStart(2, '0')
        val minuteStr = minute.toString().padStart(2, '0')
        return "$hourStr:$minuteStr"
    }

    fun joinDateAndTime(date: LocalDate, time: String): LocalDateTime {
        val (hour, minute) = time.split(":").map { it.toInt() }
        return date.atTime(hour, minute)
    }

    fun LocalDateTime.getTime(): String = formatToTimeString()

    fun LocalDateTime.isFutureTime(hour: Int, minute: Int): Boolean {
        return this.hour < hour || (this.hour == hour && this.minute < minute)
    }

    fun formatTime(hour: Int, minute: Int): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    fun timeToIntPair(time: String): Pair<Int, Int> {
        val (hour, minute) = time.split(":").map { it.toInt() }
        return hour to minute
    }

    fun String.toLocalDate(): LocalDate {
        val (day, month, year) = split("/").map { it.toInt() }
        return LocalDate(year, month, day)
    }

    fun String.toLocalDateTime(): LocalDateTime {
        val (datePart, timePart) = split(" ")
        val date = datePart.toLocalDate()
        val (hour, minute) = timeToIntPair(timePart)
        return date.atTime(hour, minute)
    }

    fun LocalDateTime.toLocalTime(): LocalTime {
        return this.time
    }

    fun Long.toLocalDate(): LocalDate {
        return Clock.System.now()
            .plus(this - Clock.System.now().toEpochMilliseconds(), DateTimeUnit.MILLISECOND)
            .toLocalDateTime(TimeZone.UTC).date
    }

    fun Long.toLocalDateTime(): LocalDateTime {
        return Clock.System.now()
            .plus(this - Clock.System.now().toEpochMilliseconds(), DateTimeUnit.MILLISECOND)
            .toLocalDateTime(TimeZone.UTC)
    }

    fun getCurrentTimeFormatted(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.UTC).formatToTimeString()
    }

    fun getCurrentDateTime(): LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.UTC)
    }

    fun LocalDate.toMillis(): Long {
        return this.atTime(0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
    }

    fun LocalDateTime.toMillis(): Long {
        return this.toInstant(TimeZone.UTC).toEpochMilliseconds()
    }

    fun Long.toMinutes(): Long = this / 60_000

    fun Long.fromMinutesToMillis(): Long = this * 60_000

    fun getColorByDelay(delay: Int): Color {
        return when {
            delay < 0 -> SoftRed
            delay < 2 -> SoftYellow
            else -> SoftGreen
        }
    }

    fun getColorByPriority(priority: Int): Color {
        return when (priority) {
            4 -> SoftRed
            3 -> SoftBlue
            2 -> SoftYellow
            else -> LightGray
        }
    }

    fun getIconByPriority(priority: Int): ImageVector {
        return when {
            priority == 3 -> Icons.Default.KeyboardArrowUp
            priority > 3 -> Icons.Default.KeyboardArrowUp
            else -> Icons.Default.KeyboardArrowDown
        }
    }

}
