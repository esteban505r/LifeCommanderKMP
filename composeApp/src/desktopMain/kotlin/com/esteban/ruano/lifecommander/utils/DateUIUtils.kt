package utils

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUIUtils {

    const val DAYS_OF_THE_WEEK = 7

    fun LocalDate.parseDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return format(formatter)
    }

    fun LocalDateTime.parseTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return format(formatter)
    }

    fun joinDateAndTime(date: LocalDate, time: String): LocalDateTime {
        val split = time.split(":")
        return date.atTime(split[0].toInt(), split[1].toInt())
    }

    fun LocalDateTime.getTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return format(formatter)
    }

    fun LocalDateTime.isFutureTime(hour: Int, minute: Int): Boolean {
        return this.hour < hour || (this.hour == hour && this.minute < minute)
    }

    fun formatTime(hour: Int, minute: Int):String = String.format("%02d:%02d", hour, minute)

    fun timeToIntPair(time: String): Pair<Int, Int> {
        val split = time.split(":")
        return Pair(split[0].toInt(), split[1].toInt())
    }

    fun String.toLocalDate(): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return LocalDate.parse(this, formatter)
    }

    fun String.toLocalDateTime(): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return LocalDateTime.parse(this, formatter)
    }

    fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.of("UTC"))
            .toLocalDate()
    }

    fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.of("UTC"))
            .toLocalDateTime()
    }


    fun getCurrentTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return LocalDateTime.now().format(formatter)
    }

    fun LocalDate.toMillis(): Long {
        return atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }

    fun LocalDateTime.toMillis(): Long {
        return atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }

    fun Long.toMinutes(): Long {
        return this / 60000
    }
    fun Long.fromMinutesToMillis(): Long {
        return this * 60000
    }
}