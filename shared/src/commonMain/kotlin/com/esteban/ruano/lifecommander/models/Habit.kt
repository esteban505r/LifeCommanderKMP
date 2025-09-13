package com.lifecommander.models

import com.esteban.ruano.lifecommander.models.HabitReminder
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime as toLocalDateTimeUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.math.sign

@Serializable
data class Habit(
    val id: String,
    val name: String,
    val note: String?,
    val dateTime: String?,     // anchor date & time
    val done: Boolean?,
    val frequency: String,     // DAILY, WEEKLY, BI_WEEKLY, MONTHLY, YEARLY, ONE_TIME
    val reminders: List<HabitReminder>? = null,
    val streak: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Comparable<Habit> {

    /**
     * Parsed anchor timestamp (creation/anchor moment).
     * This is the same reference your utils use.
     */
    val baseDateTime: LocalDateTime?
        get() = dateTime?.toLocalDateTimeUtils()

    /**
     * Compute the next occurrence at or after [now], following the same recurrence rules
     * you apply in HabitsUtils (day-of-week/month anchors, bi-weekly parity, clamping, etc.).
     * Returns null if no future occurrence exists (e.g., ONE_TIME already in the past or no base).
     */
    fun nextOccurrence(
        now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): LocalDateTime? {
        val base = baseDateTime ?: return null
        val baseTime = base.time

        return when (Frequency.valueOf(frequency.uppercase())) {
            Frequency.DAILY -> {
                val todayAt = LocalDateTime(now.date, baseTime)
                if (now <= todayAt) todayAt else LocalDateTime(now.date + DatePeriod(days = 1), baseTime)
            }

            Frequency.WEEKLY -> {
                val targetDow = base.date.dayOfWeek.isoDayNumber
                val todayDow  = now.date.dayOfWeek.isoDayNumber
                val deltaDays = ((targetDow - todayDow + 7) % 7)
                val candidate = LocalDateTime(now.date + DatePeriod(days = deltaDays), baseTime)
                if (deltaDays == 0 && now > candidate)
                    LocalDateTime(candidate.date + DatePeriod(days = 7), baseTime)
                else
                    candidate
            }


            Frequency.BI_WEEKLY -> {
                val targetDow = base.date.dayOfWeek.isoDayNumber
                val todayDow  = now.date.dayOfWeek.isoDayNumber
                val onCycle   = isSameParityWeek(base.date, now.date)

                if (onCycle) {
                    val deltaDays = ((targetDow - todayDow + 7) % 7)
                    val candidate = LocalDateTime(now.date + DatePeriod(days = deltaDays), baseTime)
                    if (deltaDays == 0 && now > candidate)
                        LocalDateTime(candidate.date + DatePeriod(days = 14), baseTime)
                    else
                        candidate
                } else {
                    val daysToNextMonday = ((8 - todayDow) % 7).let { if (it == 0) 7 else it }
                    val nextCycleWeekStart = now.date + DatePeriod(days = daysToNextMonday) // Monday
                    val candidateDate = nextCycleWeekStart + DatePeriod(days = (targetDow - 1))
                    LocalDateTime(candidateDate, baseTime)
                }
            }


            Frequency.MONTHLY -> {
                val targetDay = clampDayOfMonth(now.date.year, now.date.monthNumber, base.date.dayOfMonth)
                val candidateThisMonth = LocalDateTime(LocalDate(now.date.year, now.date.monthNumber, targetDay), baseTime)
                if (now <= candidateThisMonth) {
                    candidateThisMonth
                } else {
                    val nextMonth = (now.date.monthNumber % 12) + 1
                    val nextYear  = now.date.year + if (now.date.monthNumber == 12) 1 else 0
                    val dayNext   = clampDayOfMonth(nextYear, nextMonth, base.date.dayOfMonth)
                    LocalDateTime(LocalDate(nextYear, nextMonth, dayNext), baseTime)
                }
            }

            Frequency.YEARLY -> {
                val m = base.date.monthNumber
                val d = base.date.dayOfMonth
                val thisYearDay = clampDayOfMonth(now.date.year, m, d)
                val candidateThisYear = LocalDateTime(LocalDate(now.date.year, m, thisYearDay), baseTime)
                if (now <= candidateThisYear) {
                    candidateThisYear
                } else {
                    val nextYearDay = clampDayOfMonth(now.date.year + 1, m, d)
                    LocalDateTime(LocalDate(now.date.year + 1, m, nextYearDay), baseTime)
                }
            }

            Frequency.ONE_TIME -> {
                if (base >= now) base else null
            }
        }
    }

    /**
     * Natural ordering: earliest next occurrence first.
     * - If both have a next occurrence, compare those instants.
     * - If only one has a next occurrence, that one comes first.
     * - Fallback: compare by anchor time-of-day, then by name, then id (stable order).
     */
    override fun compareTo(other: Habit): Int {
        val n1 = this.nextOccurrence()
        val n2 = other.nextOccurrence()

        if (n1 != null && n2 != null) return n1.compareTo(n2)
        if (n1 != null) return -1
        if (n2 != null) return 1

        // No future occurrences; fallback to base time-of-day, then by name/id.
        val t1 = this.baseDateTime?.time
        val t2 = other.baseDateTime?.time
        if (t1 != null && t2 != null) return t1.compareTo(t2)
        if (t1 != null) return -1
        if (t2 != null) return 1

        val byName = this.name.compareTo(other.name).sign
        if (byName != 0) return byName
        return this.id.compareTo(other.id)
    }

    /* ------------ small helpers mirrored from your utils’ intent ------------ */

    private fun isSameParityWeek(baseDate: LocalDate, currentDate: LocalDate): Boolean {
        val baseWeekStart = startOfIsoWeek(baseDate)
        val currentWeekStart = startOfIsoWeek(currentDate)
        val daysBetween = baseWeekStart.daysUntil(currentWeekStart) // can be negative
        val weeksBetween = if (daysBetween >= 0) daysBetween / 7 else ((daysBetween - 6) / 7) // floor div
        return (weeksBetween % 2) == 0
    }

    private fun startOfIsoWeek(date: LocalDate): LocalDate {
        val offset = date.dayOfWeek.isoDayNumber - 1 // Monday -> 0
        return date - DatePeriod(days = offset)
    }

    private fun clampDayOfMonth(year: Int, month: Int, desiredDay: Int): Int {
        val last = lastDayOfMonth(year, month)
        return desiredDay.coerceAtMost(last)
    }

    private fun lastDayOfMonth(year: Int, month: Int): Int {
        val first = LocalDate(year, month, 1)
        val firstNext = first.plus(DatePeriod(months = 1))
        val last = firstNext.minus(DatePeriod(days = 1))
        return last.dayOfMonth
    }
}

/**
 * Optional: a comparator that ignores dates and compares **time-of-day only** (00:00–23:59),
 * useful for grouping today’s habits visually.
 */
object HabitTimeOfDayComparator : Comparator<Habit> {
    override fun compare(a: Habit, b: Habit): Int {
        val t1 = a.baseDateTime?.time
        val t2 = b.baseDateTime?.time
        return when {
            t1 != null && t2 != null -> t1.compareTo(t2)
            t1 != null -> -1
            t2 != null -> 1
            else -> a.name.compareTo(b.name)
        }
    }
}
