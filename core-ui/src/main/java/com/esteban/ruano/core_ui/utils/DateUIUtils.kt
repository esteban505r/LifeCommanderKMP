package com.esteban.ruano.core_ui.utils

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.esteban.ruano.core.utils.DateUtils.parseDateTime
import com.esteban.ruano.core_ui.R
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

    fun formatTime(hour: Int, minute: Int): String = String.format("%02d:%02d", hour, minute)

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

    fun LocalDateTime.toResourceStringBasedOnNow(
        context: Context,
    ): Pair<String, Color> {
        if (this.minusDays(7).toLocalDate() > LocalDate.now()) {
            return Pair(this.parseDateTime(), Color.Gray)
        }

        if (LocalDateTime.now().toLocalDate() == this.toLocalDate()) {
            val color = if (LocalDateTime.now().isAfter(this)) Color.Red else Color.Gray
            val resource = context.getString(R.string.todayAt, this.getTime())
            return Pair(resource, color)
        }

        if(LocalDate.now().minusDays(1) == this.toLocalDate()){
            val resource = context.getString(R.string.yesterdayAt, this.getTime())
            return Pair(resource, Color.Red)
        }

        if(LocalDate.now().plusDays(1) == this.toLocalDate()){
            val resource = context.getString(R.string.tomorrowAt, this.getTime())
            return Pair(resource, Color.Gray)
        }

        if (LocalDate.now() < this.toLocalDate() && LocalDate.now()
                .plusDays(7) > this.toLocalDate()
        ) {
            val first = when (this.dayOfWeek) {
                DayOfWeek.MONDAY -> context.getString(
                    R.string.at_day,
                    context.getString(R.string.monday)
                )

                DayOfWeek.TUESDAY -> context.getString(
                    R.string.at_day,
                    context.getString(R.string.tuesday)
                )

                DayOfWeek.WEDNESDAY -> context.getString(
                    R.string.at_day,
                    context.getString(R.string.wednesday)
                )

                DayOfWeek.THURSDAY -> context.getString(
                    R.string.at_day,
                    context.getString(R.string.thursday)
                )

                DayOfWeek.FRIDAY -> context.getString(
                    R.string.at_day,
                    context.getString(R.string.friday)
                )

                DayOfWeek.SATURDAY -> context.getString(
                    R.string.at_day,
                    context.getString(R.string.saturday)
                )

                DayOfWeek.SUNDAY -> context.getString(
                    R.string.at_day,
                    context.getString(R.string.sunday)
                )

                else -> context.getString(R.string.empty)
            }
            return Pair(first, Color.Gray)
        }
        if (LocalDate.now().minusDays(7) > this.toLocalDate()) {
            return Pair(this.parseDateTime(), Color.Red)
        }
        if (this.toLocalDate() < LocalDate.now() && LocalDate.now()
                .minusDays(7) < this.toLocalDate()
        ) {
            val first = when (this.dayOfWeek) {
                DayOfWeek.MONDAY -> context.getString(
                    R.string.last_day,
                    context.getString(R.string.monday)
                )

                DayOfWeek.TUESDAY -> context.getString(
                    R.string.last_day,
                    context.getString(R.string.tuesday)
                )

                DayOfWeek.WEDNESDAY -> context.getString(
                    R.string.last_day,
                    context.getString(R.string.wednesday)
                )

                DayOfWeek.THURSDAY -> context.getString(
                    R.string.last_day,
                    context.getString(R.string.thursday)
                )

                DayOfWeek.FRIDAY -> context.getString(
                    R.string.last_day,
                    context.getString(R.string.friday)
                )

                DayOfWeek.SATURDAY -> context.getString(
                    R.string.last_day,
                    context.getString(R.string.saturday)
                )

                DayOfWeek.SUNDAY -> context.getString(
                    R.string.last_day,
                    context.getString(R.string.sunday)
                )

                else -> context.getString(R.string.empty)
            }
            return Pair(first, Color.Red)
        }
        return Pair(this.parseDateTime(), Color.Red)
    }

    fun Int.toDayOfTheWeekString(context: Context): String {
        return when (this) {
            1 -> context.getString(R.string.monday)
            2 -> context.getString(R.string.tuesday)
            3 -> context.getString(R.string.wednesday)
            4 -> context.getString(R.string.thursday)
            5 -> context.getString(R.string.friday)
            6 -> context.getString(R.string.saturday)
            7 -> context.getString(R.string.sunday)
            else -> context.getString(R.string.empty)
        }

    }

    fun Int.toMonthString(context: Context): String {
        return when (this) {
            1 -> context.getString(R.string.january)
            2 -> context.getString(R.string.february)
            3 -> context.getString(R.string.march)
            4 -> context.getString(R.string.april)
            5 -> context.getString(R.string.may)
            6 -> context.getString(R.string.june)
            7 -> context.getString(R.string.july)
            8 -> context.getString(R.string.august)
            9 -> context.getString(R.string.september)
            10 -> context.getString(R.string.october)
            11 -> context.getString(R.string.november)
            12 -> context.getString(R.string.december)
            else -> context.getString(R.string.empty)
        }
    }

    fun String.toDayNumber(): Int {
        return when (this.lowercase()) {
            "monday" -> 1
            "tuesday" -> 2
            "wednesday" -> 3
            "thursday" -> 4
            "friday" -> 5
            "saturday" -> 6
            "sunday" -> 7
            else -> 0
        }
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

    fun Long.formatTime(): String {
        val hours = this / 3600
        val minutes = (this % 3600) / 60
        val seconds = this % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}