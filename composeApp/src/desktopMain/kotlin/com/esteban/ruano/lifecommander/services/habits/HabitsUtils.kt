package com.esteban.ruano.lifecommander.services.habits

import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getTime
import services.habits.models.HabitResponse
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.toLocalDate
import com.lifecommander.models.Frequency
import kotlinx.datetime.toJavaLocalTime
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

object HabitUtils {

    fun HabitResponse.time() = this.dateTime?.toLocalDateTime()?.getTime()

    fun HabitResponse.date() = this.dateTime?.toLocalDateTime()?.toLocalDate()?.formatDefault()

    fun List<HabitResponse>.findCurrentHabitResponse(): HabitResponse? {
        val currentTime = LocalTime.now()
        return this.minByOrNull {
            val HabitResponseTime = it.dateTime?.toLocalDateTime()?.time
            HabitResponseTime?.let { ChronoUnit.MINUTES.between(currentTime, it.toJavaLocalTime()).absoluteValue } ?: Long.MAX_VALUE
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
                this.dateTime?.toLocalDateTime()?.toLocalDate()?.formatDefault()
            }

            Frequency.ONE_TIME -> {
                this.dateTime?.toLocalDateTime()?.formatDefault()
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