package com.esteban.ruano.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import com.esteban.ruano.database.entities.HabitTrack
import com.esteban.ruano.models.habits.HabitDTO
import com.lifecommander.models.Frequency

object HabitUtils {
    fun isDone(habit: HabitDTO, tracking: HabitTrack?, date: LocalDate): Boolean {
        println("=== HABITUTILS DEBUG ===")
        println("Habit: ${habit.name}, Frequency: ${habit.frequency}")
        println("Date: $date")
        println("Tracking: ${tracking?.doneDateTime}")
        println("Tracking date: ${tracking?.doneDateTime?.date}")
        
        val result = when (habit.frequency.uppercase()) {

            Frequency.DAILY.value.uppercase() -> {
                val trackingNull = tracking == null
                val dateMismatch = tracking?.doneDateTime?.date != date
                val finalResult = !(trackingNull || dateMismatch)
                println("  DAILY: tracking null: $trackingNull, date mismatch: $dateMismatch, result: $finalResult")
                finalResult
            }

            Frequency.WEEKLY.value.uppercase() -> {
                if (tracking == null) {
                    println("  WEEKLY: tracking null, result: false")
                    false
                } else if (haveMoreThanAWeekOfDifference(
                        tracking.date.date,
                        date
                    ) || tracking.date.dayOfWeek == date.dayOfWeek && tracking.doneDateTime.date != date
                ) {
                    println("  WEEKLY: more than week difference or same day but different date, result: false")
                    false
                } else {
                    println("  WEEKLY: result: true")
                    true
                }
            }

            Frequency.MONTHLY.value.uppercase() -> {
                if (tracking == null) {
                    println("  MONTHLY: tracking null, result: false")
                    false
                } else if (haveMoreThanAMonthOfDifference(tracking.date.date, date) || tracking.date.dayOfMonth == date.dayOfMonth && tracking.date.date != date) {
                    println("  MONTHLY: more than month difference or same day but different date, result: false")
                    false
                } else {
                    println("  MONTHLY: result: true")
                    true
                }
            }

            Frequency.YEARLY.value.uppercase() -> {
                if (tracking == null) {
                    println("  YEARLY: tracking null, result: false")
                    false
                } else if (haveMoreThanAYearOfDifference(tracking.date.date, date) || tracking.date.dayOfYear == date.dayOfYear && tracking.date.date != date) {
                    println("  YEARLY: more than year difference or same day but different date, result: false")
                    false
                } else {
                    println("  YEARLY: result: true")
                    true
                }
            }

            else -> {
                println("  UNKNOWN frequency: ${habit.frequency}, result: false")
                false
            }
        }
        
        println("Final result: $result")
        println("=== END HABITUTILS DEBUG ===")
        return result
    }
}