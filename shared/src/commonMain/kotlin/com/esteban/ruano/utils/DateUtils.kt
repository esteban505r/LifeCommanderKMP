package com.esteban.ruano.utils

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.*

object DateUtils {

    // Parsing functions
    fun parseDate(dateString: String): LocalDate {
        val parts = dateString.split("/")
        return LocalDate(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
    }

    fun parseDateTime(dateString: String): LocalDateTime {
        val (datePart, timePart) = dateString.split(" ")
        val date = parseDate(datePart)
        val time = parseTime(timePart)
        return LocalDateTime(date, time)
    }

    fun parseTime(timeString: String): LocalTime {
        val (hours, minutes) = timeString.split(":").map { it.toInt() }
        return LocalTime(hours, minutes)
    }

    // Formatting functions
    fun formatDate(date: LocalDate): String {
        return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
    }

    fun formatDateTime(dateTime: LocalDateTime): String {
        return "${formatDate(dateTime.date)} ${formatTime(dateTime.time)}"
    }

    fun formatTime(time: LocalTime): String {
        return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    }

    // Extension functions
    fun String.toLocalTime(): LocalTime {
        return parseTime(this)
    }

    fun String.fromDateToLong(): Long {
        return parseDateTime(this).toInstant(TimeZone.UTC).toEpochMilliseconds()
    }

    fun LocalDateTime.toLocalDate(): LocalDate {
        return this.date
    }

    fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    }

    // Time difference functions
    fun haveMoreThanAWeekOfDifference(date1: LocalDate, date2: LocalDate): Boolean {
        return date1.daysUntil(date2) > 7
    }

    fun haveMoreThanAMonthOfDifference(date1: LocalDate, date2: LocalDate): Boolean {
        return date1.daysUntil(date2) > 30
    }

    fun haveMoreThanAYearOfDifference(date1: LocalDate, date2: LocalDate): Boolean {
        return date1.daysUntil(date2) > 365
    }

    // Time-based calculations
    fun timeToIntPair(time: String): Pair<Int, Int> {
        val (hours, minutes) = time.split(":").map { it.toInt() }
        return Pair(hours, minutes)
    }

    fun formatTime(hour: Int, minute: Int): String {
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    fun Long.parseTime(): String {
        val hours = this / 60
        val minutes = this % 60
        return formatTime(hours.toInt(), minutes.toInt())
    }

    fun Long.toMinutes(): Long {
        return this / 60000
    }

    fun Long.fromMinutesToMillis(): Long {
        return this * 60000
    }

    fun LocalDateTime.toResourceStringBasedOnNow(
        context: Context,
    ): Pair<String, Color> {
        if (this.minusDays(7).toLocalDate() > LocalDate.now()) {
            return Pair(this.parseDateTime(), DarkGray)
        }

        if (LocalDateTime.now().toLocalDate() == this.toLocalDate()) {
            val color = if (LocalDateTime.now().isAfter(this)) SoftRed else DarkGray
            val resource = context.getString(MR.string.todayAt, this.getTime())
            return Pair(resource, color)
        }

        if(LocalDate.now().minusDays(1) == this.toLocalDate()){
            val resource = context.getString(MR.string.yesterdayAt, this.getTime())
            return Pair(resource, SoftRed)
        }

        if(LocalDate.now().plusDays(1) == this.toLocalDate()){
            val resource = context.getString(MR.string.tomorrowAt, this.getTime())
            return Pair(resource, DarkGray)
        }

        if (LocalDate.now() < this.toLocalDate() && LocalDate.now()
                .plusDays(7) > this.toLocalDate()
        ) {
            val first = when (this.dayOfWeek) {
                DayOfWeek.MONDAY -> context.getString(
                    MR.string.at_day,
                    context.getString(MR.string.monday)
                )

                DayOfWeek.TUESDAY -> context.getString(
                    MR.string.at_day,
                    context.getString(MR.string.tuesday)
                )

                DayOfWeek.WEDNESDAY -> context.getString(
                    MR.string.at_day,
                    context.getString(MR.string.wednesday)
                )

                DayOfWeek.THURSDAY -> context.getString(
                    MR.string.at_day,
                    context.getString(MR.string.thursday)
                )

                DayOfWeek.FRIDAY -> context.getString(
                    MR.string.at_day,
                    context.getString(MR.string.friday)
                )

                DayOfWeek.SATURDAY -> context.getString(
                    MR.string.at_day,
                    context.getString(MR.string.saturday)
                )

                DayOfWeek.SUNDAY -> context.getString(
                    MR.string.at_day,
                    context.getString(MR.string.sunday)
                )

                else -> context.getString(MR.string.empty)
            }
            return Pair(first, DarkGray)
        }
        if (LocalDate.now().minusDays(7) > this.toLocalDate()) {
            return Pair(this.parseDateTime(), SoftRed)
        }
        if (this.toLocalDate() < LocalDate.now() && LocalDate.now()
                .minusDays(7) < this.toLocalDate()
        ) {
            val first = when (this.dayOfWeek) {
                DayOfWeek.MONDAY -> context.getString(
                    MR.string.last_day,
                    context.getString(MR.string.monday)
                )

                DayOfWeek.TUESDAY -> context.getString(
                    MR.string.last_day,
                    context.getString(MR.string.tuesday)
                )

                DayOfWeek.WEDNESDAY -> context.getString(
                    MR.string.last_day,
                    context.getString(MR.string.wednesday)
                )

                DayOfWeek.THURSDAY -> context.getString(
                    MR.string.last_day,
                    context.getString(MR.string.thursday)
                )

                DayOfWeek.FRIDAY -> context.getString(
                    MR.string.last_day,
                    context.getString(MR.string.friday)
                )

                DayOfWeek.SATURDAY -> context.getString(
                    MR.string.last_day,
                    context.getString(MR.string.saturday)
                )

                DayOfWeek.SUNDAY -> context.getString(
                    MR.string.last_day,
                    context.getString(MR.string.sunday)
                )

                else -> context.getString(MR.string.empty)
            }
            return Pair(first, SoftRed)
        }
        return Pair(this.parseDateTime(), SoftRed)
    }
} 