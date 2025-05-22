package com.esteban.ruano.utils
import com.lifecommander.models.Frequency
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter

fun parseDate(dateString: String): LocalDate {
    val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val date = java.time.LocalDate.parse(dateString, inputFormatter)
    val formattedDate = date.format(outputFormatter)
    return LocalDate.parse(formattedDate)
}

fun parseDateTime(dateString: String): LocalDateTime {
    val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val date = java.time.LocalDateTime.parse(dateString, inputFormatter)
    val formattedDate = date.format(outputFormatter)
    return LocalDateTime.parse(formattedDate)
}

fun parseDateTimeWithSeconds(dateString: String): LocalDateTime {
    val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    val outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val date = java.time.LocalDateTime.parse(dateString, inputFormatter)
    val formattedDate = date.format(outputFormatter)
    return LocalDateTime.parse(formattedDate)
}

fun formatDate(date: LocalDate): String {
    val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val dateString = date.toString()
    val date2 = java.time.LocalDate.parse(dateString, inputFormatter)
    return date2.format(outputFormatter)
}

fun formatDateTime(date: LocalDateTime): String {
    val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val dateString = date.toString()
    val date2 = java.time.LocalDateTime.parse(dateString, inputFormatter)
    return date2.format(outputFormatter)
}

fun formatTime(date: LocalDateTime): String {
    val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val outputFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateString = date.toString()
    val date2 = java.time.LocalDateTime.parse(dateString, inputFormatter)
    return date2.format(outputFormatter)
}

fun formatTime(time: LocalTime):String{
    val inputFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    val outputFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val timeString = time.toString()
    val time2 = java.time.LocalTime.parse(timeString, inputFormatter)
    return time2.format(outputFormatter)
}

fun Int.toDayOfWeek(): DayOfWeek {
    return when (this) {
        1 -> DayOfWeek.MONDAY
        2 -> DayOfWeek.TUESDAY
        3 -> DayOfWeek.WEDNESDAY
        4 -> DayOfWeek.THURSDAY
        5 -> DayOfWeek.FRIDAY
        6 -> DayOfWeek.SATURDAY
        7 -> DayOfWeek.SUNDAY
        else -> throw IllegalArgumentException("Invalid day of week")
    }
}

fun String.toLocalTime(): LocalTime {
    val inputFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val time = java.time.LocalTime.parse(this, inputFormatter)
    return LocalTime.parse(time.toString())
}

fun String.fromDateToLong(): Long {
    val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val date = java.time.LocalDateTime.parse(this, inputFormatter)
    return date.toEpochSecond(java.time.ZoneOffset.UTC)
}

fun LocalDateTime.toLocalDate(): LocalDate {
    val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val dateString = this.toString()
    val date = java.time.LocalDateTime.parse(dateString, inputFormatter)
    val formattedDate = date.format(outputFormatter)
    return LocalDate.parse(formattedDate)
}

fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
}

fun haveMoreThanAWeekOfDifference(date1: LocalDate, date2: LocalDate): Boolean {
    return date1.daysUntil(date2) > 7
}

fun haveMoreThanAMonthOfDifference(date1: LocalDate, date2: LocalDate): Boolean {
    return date1.daysUntil(date2) > 30
}

fun haveMoreThanAYearOfDifference(date1: LocalDate, date2: LocalDate): Boolean {
    return date1.daysUntil(date2) > 365
}