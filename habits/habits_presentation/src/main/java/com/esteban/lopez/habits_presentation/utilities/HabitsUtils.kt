package com.esteban.ruano.habits_presentation.utilities

import android.content.Context
import com.esteban.ruano.core.utils.DateUtils.parseDate
import com.esteban.ruano.core.utils.DateUtils.parseDateTime
import com.esteban.ruano.core.utils.DateUtils.parseTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.getTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.timeToIntPair
import com.esteban.ruano.core_ui.utils.DateUIUtils.toDayOfTheWeekString
import com.esteban.ruano.core_ui.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.toMonthString
import com.esteban.ruano.habits_domain.model.Frequency
import com.esteban.ruano.habits_domain.model.Habit
import com.esteban.ruano.core_ui.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

object HabitsUtils {

    fun Habit.time() = this.dateTime?.toLocalDateTime()?.getTime()

    fun Habit.timeDoingIt() = this.dateTime?.toLocalDateTime()?.toLocalTime()
        ?.let { ChronoUnit.SECONDS.between(it, LocalTime.now()).absoluteValue }

    fun Habit.date() = this.dateTime?.toLocalDateTime()?.toLocalDate()?.parseDate()

    fun getStringResourceByCurrentHabit(context: Context,habit:Habit?, timeDoing:String):String{
        val dateTime = habit?.dateTime?.toLocalDateTime()
        return if(dateTime?.toLocalTime()?.isBefore(LocalTime.now()) == true){
            if(habit.frequency?.uppercase() != Frequency.DAILY.value) {
                return when (habit.frequency) {
                    Frequency.WEEKLY.value -> {
                        if (dateTime.dayOfWeek == LocalDateTime.now().dayOfWeek) {
                            context.getString(R.string.youvebeen_doing_this_for, timeDoing)
                        } else {
                            context.getString(R.string.youdidnt_do_this_at, dateTime.toLocalDate().dayOfWeek.value.toDayOfTheWeekString(context))
                        }
                    }
                    Frequency.MONTHLY.value -> {
                        if (dateTime.toLocalDate().month == LocalDateTime.now().month) {
                            context.getString(R.string.youvebeen_doing_this_for, timeDoing)
                        } else {
                            context.getString(R.string.youdidnt_do_this_at, dateTime.toLocalDate().month.value.toMonthString(context))
                        }
                    }
                    Frequency.YEARLY.value -> {
                        if (dateTime.toLocalDate().dayOfYear == LocalDateTime.now().dayOfYear) {
                            context.getString(R.string.youvebeen_doing_this_for, timeDoing)
                        } else {
                            context.getString(R.string.youdidnt_do_this_at, dateTime.toLocalDate().parseDate())
                        }
                    }

                    else -> {
                        context.getString(R.string.youvebeen_doing_this_for, timeDoing)
                    }
                }
            }
            else {
                context.getString(R.string.youvebeen_doing_this_for, timeDoing)
            }
        }
        else{
            context.getString(R.string.youllstart_doing_this_in,timeDoing)
        }
    }

    fun List<Habit>.findCurrentHabit(): Habit? {
        val currentTime = LocalDateTime.now()
        val filtered = this.filter {
            it.done == false &&
            (it.frequency?.uppercase() == Frequency.DAILY.value ||
            (it.frequency?.uppercase() == Frequency.WEEKLY.value && it.dateTime?.toLocalDateTime()?.dayOfWeek == currentTime.dayOfWeek) ||
            (it.frequency?.uppercase() == Frequency.MONTHLY.value && it.dateTime?.toLocalDateTime()?.toLocalDate()?.month == currentTime.month) ||
            (it.frequency?.uppercase() == Frequency.YEARLY.value && it.dateTime?.toLocalDateTime()?.toLocalDate()?.dayOfYear == currentTime.dayOfYear))
            && it.dateTime?.toLocalDateTime()?.toLocalTime()?.isBefore(LocalTime.now()) == true
        }
        return filtered.minByOrNull { habit ->
            val habitTime = habit.dateTime?.toLocalDateTime()?.toLocalTime()
            habitTime?.let { ChronoUnit.MINUTES.between(currentTime, it).absoluteValue } ?: Long.MAX_VALUE
        } ?: findLastOverdueHabit()
    }


    fun List<Habit>.findLastOverdueHabit():Habit?{
        val moreThanDaily = this.firstOrNull{
            it.done == false && it.frequency?.uppercase() != Frequency.DAILY.value
                    && it.dateTime?.toLocalDateTime()?.toLocalTime()?.isBefore(LocalTime.now()) == true
        }
        return moreThanDaily ?: this.maxByOrNull {
            it.dateTime?.toLocalDateTime()?.toLocalTime()?.let { time ->
                ChronoUnit.MINUTES.between(LocalDateTime.now(), time).absoluteValue
            } ?: Long.MIN_VALUE
        }
    }

    fun Habit.getTimeText(
        context: Context
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
                        ?.toLocalDate()?.dayOfWeek?.value?.toDayOfTheWeekString(context)
                }
            }

            Frequency.MONTHLY -> {
                this.dateTime?.toLocalDateTime()?.toLocalDate()?.month?.value?.toMonthString(context)
            }

            Frequency.YEARLY -> {
                this.dateTime?.toLocalDateTime()?.toLocalDate()?.parseDate()
            }

            Frequency.ONE_TIME -> {
                this.dateTime?.toLocalDateTime()?.parseDateTime()
            }
        } ?: ""
    }

    fun Habit.getDelay(): Int {
        val now = LocalDateTime.now()
        when(frequency?.uppercase()){
            Frequency.WEEKLY.value -> {
                val dayOfWeek = dateTime?.toLocalDateTime()?.dayOfWeek?.value ?: 1
                val currentDayOfWeek = now.dayOfWeek.value
                return if(dayOfWeek >= currentDayOfWeek){
                    5
                } else if(dayOfWeek < currentDayOfWeek){
                    1
                } else{
                    0
                }
            }

        }
        val time = timeToIntPair(dateTime?.toLocalDateTime()?.parseTime() ?: "00:00")
        return Duration.between(
            now,LocalDateTime.of(now.year, now.month, now.dayOfMonth, time.first, time.second)
        ).toHours().toInt()
    }
}