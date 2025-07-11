package utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import models.TimeTypes
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object DateUtils {
    fun String.toLocalDate(): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return LocalDate.parse(this, formatter)
    }

    fun LocalDateTime.parseDateTime(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return format(formatter)
    }

    fun timeToIntPair(time: String): Pair<Int, Int> {
        val split = time.split(":")
        return Pair(split[0].toInt(), split[1].toInt())
    }

//    fun String.toLocalDateTime(): LocalDateTime {
//        if(this.count()==5){
//            val defaultDate = LocalDate.now().parseDate()
//            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
//            return LocalDateTime.parse("$defaultDate $this", formatter)
//        }
//        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
//        return LocalDateTime.parse(this, formatter)
//    }

    fun LocalDate.toLocalDateTime(time: LocalTime? = null):LocalDateTime{
        return if(time!=null){
            LocalDateTime.of(this, time)
        } else {
            LocalDateTime.of(this, LocalTime.now())
        }
    }

    fun LocalDateTime.getTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return format(formatter)
    }

    fun LocalTime.getTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return format(formatter)
    }

    fun String.toLocalTime(): LocalTime {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return LocalTime.parse(this, formatter)
    }

    fun LocalDate.parseDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return format(formatter)
    }

    fun Long.parseTime(): String {
        val hours = this / 60
        val minutes = this % 60
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    }

    //From millis
    fun Long.getTimeSeparated(): Map<TimeTypes,Long> {
        val seconds = this / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return mapOf(
            TimeTypes.HOUR to hours % 24,
            TimeTypes.MINUTE to minutes % 60,
            TimeTypes.SECOND to seconds % 60
        )
    }

    fun Duration.format(): String {
        val duration = this
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        val seconds = duration.toSecondsPart()
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }


    fun kotlinx.datetime.LocalDateTime.calculateTimeRemaining(currentTimeMillis: Long): String {
        val userTimeZone = TimeZone.currentSystemDefault()
        val targetTime = this.toInstant(userTimeZone)
        val diffMillis = targetTime.toEpochMilliseconds() - currentTimeMillis

        return when {
            diffMillis < 0 -> "Overdue"
            diffMillis < 60_000 -> "Less than a minute"
            diffMillis < 3_600_000 -> "${diffMillis / 60_000} minutes"
            diffMillis < 86_400_000 -> "${diffMillis / 3_600_000} hours"
            else -> "${diffMillis / 86_400_000} days"
        }
    }
}