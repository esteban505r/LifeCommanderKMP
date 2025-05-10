package com.esteban.ruano.tasks_domain.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class TaskFilters(val value: String) {
    TODAY("today"),
    TOMORROW("tomorrow",),
    NEXT_WEEK("next_week",),
    THIS_MONTH("this_month",),
    NO_DUE_DATE("no_due_date",),
    ALL("all"),;

    override fun toString(): String {
        return value
    }

    fun getDateRangeByFilter(): Pair<String?,String?> {
        val dateRange =  when (this) {
            TODAY -> Pair(LocalDate.now(), LocalDate.now())
            TOMORROW -> Pair(LocalDate.now().plusDays(1), LocalDate.now().plusDays(1))
            NEXT_WEEK -> {
                val daysUntilNextMonday = DayOfWeek.MONDAY.value - LocalDate.now().dayOfWeek.value + 7 % 7
                Pair(LocalDate.now().plusDays(daysUntilNextMonday.toLong()), LocalDate.now().plusDays(daysUntilNextMonday.toLong() + 6))
            }
            THIS_MONTH -> {
                val firstDayOfMonth = LocalDate.now().withDayOfMonth(1)
                val lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
                Pair(firstDayOfMonth, lastDayOfMonth)
            }
            NO_DUE_DATE -> Pair(null, null)
            else -> Pair(LocalDate.now(), LocalDate.now())
        }
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return Pair(dateRange.first?.format(formatter), dateRange.second?.format(formatter))
    }
}