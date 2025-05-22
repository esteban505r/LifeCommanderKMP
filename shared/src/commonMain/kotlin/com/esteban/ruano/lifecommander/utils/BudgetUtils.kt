package com.esteban.ruano.lifecommander.utils

import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.lifecommander.models.Frequency
import kotlinx.datetime.*

fun getCurrentPeriodForUnbudgeted(referenceDate: LocalDate): Pair<LocalDate, LocalDate> {
    val start = LocalDate(referenceDate.year, referenceDate.month, 1)
    val end = start.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
    return Pair(start, end)
}
fun getCurrentPeriod(budget: Budget, referenceDate: LocalDate): Pair<LocalDate, LocalDate> {
    val startDate = budget.startDate.toLocalDate()
    val frequency = budget.frequency

    val result = when (frequency) {
        Frequency.ONE_TIME -> {
            val endDate = budget.endDate?.toLocalDate() ?: startDate
            Pair(startDate, endDate)
        }
        Frequency.WEEKLY -> {
            val daysBetween = startDate.daysUntil(referenceDate)
            val weeksBetween = daysBetween / 7
            val periodStart = startDate.plus(DatePeriod(days = weeksBetween * 7))
            val periodEnd = periodStart.plus(DatePeriod(days = 6))
            Pair(periodStart, periodEnd)
        }
        Frequency.MONTHLY -> {
            val monthsBetween = startDate.monthsUntil(referenceDate)
            val periodStart = startDate.plus(DatePeriod(months = monthsBetween))
            val periodEnd = periodStart.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
            Pair(periodStart, periodEnd)
        }
        Frequency.YEARLY -> {
            val yearsBetween = startDate.yearsUntil(referenceDate)
            val periodStart = startDate.plus(DatePeriod(years = yearsBetween))
            val periodEnd = periodStart.plus(DatePeriod(years = 1)).minus(DatePeriod(days = 1))
            Pair(periodStart, periodEnd)
        }

        Frequency.DAILY -> {
            val daysBetween = startDate.daysUntil(referenceDate)
            val periodStart = startDate.plus(DatePeriod(days = daysBetween))
            val periodEnd = periodStart
            Pair(periodStart, periodEnd)
        }
        Frequency.BI_WEEKLY -> {
            val daysBetween = startDate.daysUntil(referenceDate)
            val weeksBetween = daysBetween / 14
            val periodStart = startDate.plus(DatePeriod(days = weeksBetween * 14))
            val periodEnd = periodStart.plus(DatePeriod(days = 13))
            Pair(periodStart, periodEnd)
        }
    }
    println("startDate: ${result.first}, endDate: ${result.second}")
    return result
}
