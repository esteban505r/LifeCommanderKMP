package com.esteban.ruano.lifecommander.models

import com.esteban.ruano.utils.DateUIUtils.formatDefault
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

import kotlinx.datetime.*


enum class TaskFilters(val value: String) {
    TODAY("today"),
    TOMORROW("tomorrow"),
    NEXT_WEEK("next_week"),
    THIS_MONTH("this_month"),
    NO_DUE_DATE("no_due_date"),
    ALL("all");

    override fun toString(): String {
        return value
    }

    fun getDateRangeByFilter(): Pair<String?, String?> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dateRange = when (this) {
            TODAY -> Pair(now, now)
            TOMORROW -> Pair(now.plus(1, DateTimeUnit.DAY), now.plus(1, DateTimeUnit.DAY))
            NEXT_WEEK -> {
                val daysUntilNextMonday = (DayOfWeek.MONDAY.ordinal - now.dayOfWeek.ordinal + 7) % 7
                val startOfNextWeek = now.plus(daysUntilNextMonday.toLong(), DateTimeUnit.DAY)
                val endOfNextWeek = startOfNextWeek.plus(6, DateTimeUnit.DAY)
                Pair(startOfNextWeek, endOfNextWeek)
            }
            THIS_MONTH -> {
                val firstDayOfMonth = LocalDate(now.year, now.monthNumber, 1)
                val firstDayOfNextMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH)
                val lastDayOfMonth = firstDayOfNextMonth.minus(1, DateTimeUnit.DAY)
                Pair(firstDayOfMonth, lastDayOfMonth)
            }
            NO_DUE_DATE -> Pair(null, null)
            else -> Pair(now, now)
        }
        val formattedFirst = dateRange.first?.formatDefault()
        val formattedSecond = dateRange.second?.formatDefault()
        return Pair(formattedFirst, formattedSecond)
    }
}
