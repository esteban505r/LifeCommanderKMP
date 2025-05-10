package com.esteban.ruano.core.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    fun LocalDate.parseDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return format(formatter)
    }

    fun LocalDateTime.parseTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return format(formatter)
    }

    fun LocalDateTime.parseDateTime(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return format(formatter)
    }

    fun formatTime(hour: Int, minute: Int):String = String.format("%02d:%02d", hour, minute)

    fun formatElapsedTime(elapsedTime: Long): String {
        val minutes = (elapsedTime / 60).toInt()
        val seconds = (elapsedTime % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }


    fun timeToIntPair(time: String): Pair<Int, Int> {
        val split = time.split(":")
        return Pair(split[0].toInt(), split[1].toInt())
    }

    fun String.toLocalDate(): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return LocalDate.parse(this, formatter)
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

    fun Long.formatTime(): String {
        val millisUntilFinished = this
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
        return String.format(Locale.US,"%02d:%02d", minutes, seconds)
    }

    fun String.toLocalTime(): LocalTime {
        val formatter = DateTimeFormatterBuilder()
            .appendPattern("HH:mm")
            .optionalStart()
            .appendPattern(":ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .optionalEnd()
            .toFormatter()

        return LocalTime.parse(this, formatter)
    }

    fun getCurrentTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return LocalDateTime.now().format(formatter)
    }

    fun LocalDate.toMillis(): Long {
        return atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }
}