package services.HabitResponses

import services.habits.models.Frequency
import services.habits.models.HabitResponse
import utils.DateUIUtils.toLocalDateTime
import utils.DateUtils.getTime
import utils.DateUtils.parseDate
import utils.DateUtils.parseDateTime
import utils.DateUtils.toLocalDateTime
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

object HabitUtils {

    fun HabitResponse.time() = this.dateTime?.toLocalDateTime()?.getTime()

    fun HabitResponse.date() = this.dateTime?.toLocalDateTime()?.toLocalDate()?.parseDate()

    fun List<HabitResponse>.findCurrentHabitResponse(): HabitResponse? {
        val currentTime = LocalTime.now()
        return this.minByOrNull {
            val HabitResponseTime = it.dateTime?.toLocalDateTime()?.toLocalTime()
            HabitResponseTime?.let { ChronoUnit.MINUTES.between(currentTime, it).absoluteValue } ?: Long.MAX_VALUE
        }
    }

    fun HabitResponse.getTimeText(
    ): String {
        val frequency = Frequency.valueOf(this.frequency?.uppercase() ?: "DAILY")
        return when (frequency) {
            Frequency.DAILY -> {
                this.dateTime?.toLocalDateTime()?.getTime()
            }

            Frequency.WEEKLY -> {
                if(this.dateTime?.toLocalDateTime()?.dayOfWeek == LocalDateTime.now().dayOfWeek){
                    this.dateTime?.toLocalDateTime()?.getTime()
                }
                else {
                    this.dateTime?.toLocalDateTime()
                        ?.toLocalDate()?.dayOfWeek?.value?.toDayOfTheWeekString()
                }
            }

            Frequency.MONTHLY -> {
                this.dateTime?.toLocalDateTime()?.toLocalDate()?.month?.value?.toMonthString()
            }

            Frequency.YEARLY -> {
                this.dateTime?.toLocalDateTime()?.toLocalDate()?.parseDate()
            }

            Frequency.ONE_TIME -> {
                this.dateTime?.toLocalDateTime()?.parseDateTime()
            }
        } ?: ""
    }

    fun Int.toDayOfTheWeekString(): String {
        return when (this) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> ""
        }

    }

    fun Int.toMonthString(): String {
        return when (this) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> ""
        }
    }

}