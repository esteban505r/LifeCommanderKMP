package com.esteban.ruano.utils

import androidx.compose.ui.graphics.Color
import com.esteban.ruano.MR
import com.esteban.ruano.ui.LightGray
import com.esteban.ruano.ui.SoftRed
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.lifecommander.models.Frequency
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.datetime.*
import kotlin.time.Duration

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

    fun LocalDateTime.parseDateTime(): String {
        val day = this.date.dayOfMonth.toString().padStart(2, '0')
        val month = this.date.monthNumber.toString().padStart(2, '0')
        val year = this.date.year.toString()
        val hour = this.hour.toString().padStart(2, '0')
        val minute = this.minute.toString().padStart(2, '0')
        return "$day/$month/$year $hour:$minute"
    }

    fun LocalDate.parseDate(): String {
        val day = dayOfMonth.toString().padStart(2, '0')
        val month = monthNumber.toString().padStart(2, '0')
        val year = year.toString()
        return "$day/$month/$year"
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


    fun LocalDateTime.toResourceStringBasedOnNow(): Pair<StringDesc, Color> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        if (this.date > today.plus(7, DateTimeUnit.DAY)) {
            return Pair(StringDesc.Raw(this.formatDefault()), LightGray)
        }

        if (this.date == today) {
            val color = if (now > this) SoftRed else LightGray
            val resource = StringDesc.ResourceFormatted(MR.strings.todayAt, this.time.formatDefault())
            return Pair(resource, color)
        }

        if (this.date == today.minus(1, DateTimeUnit.DAY)) {
            val resource = StringDesc.ResourceFormatted(MR.strings.yesterdayAt, this.time.formatDefault())
            return Pair(resource, SoftRed)
        }

        if (this.date == today.plus(1, DateTimeUnit.DAY)) {
            val resource = StringDesc.ResourceFormatted(MR.strings.tomorrowAt, this.time.formatDefault())
            return Pair(resource, LightGray)
        }

        if (this.date > today && this.date < today.plus(7, DateTimeUnit.DAY)) {
            val dayString = this.date.dayOfWeek.toDayStringRes()
            val resource = StringDesc.ResourceFormatted(MR.strings.at_day, StringDesc.Resource(dayString))
            return Pair(resource, LightGray)
        }

        if (this.date < today.minus(7, DateTimeUnit.DAY)) {
            return Pair(StringDesc.Raw(this.formatDefault()), SoftRed)
        }

        if (this.date < today && this.date > today.minus(7, DateTimeUnit.DAY)) {
            val dayString = this.date.dayOfWeek.toDayStringRes()
            val resource = StringDesc.ResourceFormatted(MR.strings.last_day, StringDesc.Resource(dayString))
            return Pair(resource, SoftRed)
        }

        return Pair(StringDesc.Raw(this.formatDefault()), SoftRed)
    }

    fun getPeriodEndDate(startDate: LocalDate, frequency: Frequency): LocalDate{
        return when (frequency) {
            Frequency.ONE_TIME -> startDate
            Frequency.DAILY -> startDate.plus(1, DateTimeUnit.DAY)
            Frequency.WEEKLY -> startDate.plus(1, DateTimeUnit.WEEK)
            Frequency.BI_WEEKLY -> startDate.plus(2, DateTimeUnit.WEEK)
            Frequency.MONTHLY -> startDate.plus(1, DateTimeUnit.MONTH)
            Frequency.YEARLY -> startDate.plus(1, DateTimeUnit.YEAR)
        }
    }


    private fun DayOfWeek.toDayStringRes(): StringResource{
        return when (this) {
            DayOfWeek.MONDAY -> MR.strings.monday
            DayOfWeek.TUESDAY -> MR.strings.tuesday
            DayOfWeek.WEDNESDAY -> MR.strings.wednesday
            DayOfWeek.THURSDAY -> MR.strings.thursday
            DayOfWeek.FRIDAY -> MR.strings.friday
            DayOfWeek.SATURDAY -> MR.strings.saturday
            DayOfWeek.SUNDAY -> MR.strings.sunday
            else -> MR.strings.unknown
        }
    }


    fun Duration.formatDefault(): String {
        val totalSeconds = this.inWholeSeconds
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            "${pad(hours)}:${pad(minutes)}:${pad(seconds)}"
        } else {
            "${pad(minutes)}:${pad(seconds)}"
        }
    }

    private fun pad(value: Long): String = value.toString().padStart(2, '0')

} 