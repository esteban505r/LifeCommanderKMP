package com.esteban.ruano.utils

import haveMoreThanAMonthOfDifference
import haveMoreThanAWeekOfDifference
import haveMoreThanAYearOfDifference
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import com.esteban.ruano.database.entities.HabitTrack
import com.esteban.ruano.database.models.Frequency
import com.esteban.ruano.models.habits.HabitDTO

object HabitUtils {
    fun isDone(habit: HabitDTO, tracking: HabitTrack?, date: LocalDate): Boolean {
        return when (habit.frequency) {
            Frequency.DAILY.value -> {
                !(tracking == null || tracking.doneDateTime.date != date)
            }

            Frequency.WEEKLY.value -> {
                if (tracking == null) {
                    false
                } else if (haveMoreThanAWeekOfDifference(
                        tracking.date.date,
                        date
                    ) || tracking.date.dayOfWeek == date.dayOfWeek && tracking.doneDateTime.date != date
                ) {
                    false
                } else {
                    true
                }
            }

            Frequency.MONTHLY.value -> {
                if (tracking == null) {
                    false
                } else if (haveMoreThanAMonthOfDifference(tracking.date.date, date) || tracking.date.dayOfMonth == date.dayOfMonth && tracking.date.date != date) {
                    false
                } else {
                    true
                }
            }

            Frequency.YEARLY.value -> {
                if (tracking == null) {
                    false
                } else if (haveMoreThanAYearOfDifference(tracking.date.date, date) || tracking.date.dayOfYear == date.dayOfYear && tracking.date.date != date) {
                    false
                } else {
                    true
                }
            }

            else -> false
        }
    }
}