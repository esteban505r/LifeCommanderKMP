package utils

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

    fun Long.getTimeSeparated(): Map<TimeTypes,Long> {
        val hours = this / 3600
        val minutes = (this % 3600) / 60
        val seconds = this % 60
        return mapOf(TimeTypes.HOUR to hours, TimeTypes.MINUTE to minutes, TimeTypes.SECOND to seconds)
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
}