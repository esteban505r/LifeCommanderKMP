package com.esteban.ruano.service

import com.esteban.ruano.database.entities.Transactions
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.FinanceStatisticsDTO
import com.esteban.ruano.lifecommander.models.MonthlyTrendDTO
import kotlinx.datetime.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class FinanceStatisticsService(
    private val transactionService: TransactionService,
    private val accountService: AccountService
) {
    fun getFinanceStatistics(userId: Int): FinanceStatisticsDTO {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val startOfWeek = today.minus(DatePeriod(days = today.dayOfWeek.ordinal))
        val endOfWeek = startOfWeek.plus(DatePeriod(days = 6))
        
        val startOfMonth = LocalDate(today.year, today.monthNumber, 1)
        val endOfMonth = startOfMonth.plus(DatePeriod(months = 1)).minus(1, DateTimeUnit.DAY)
        
        // Get all transactions for the current month
        val allTransactions = transaction {
            Transactions.selectAll().where {
                (Transactions.user eq userId) and
                (Transactions.status eq Status.ACTIVE) and
                (Transactions.date.date() greaterEq startOfMonth) and
                (Transactions.date.date() lessEq endOfMonth)
            }.map {
                Triple(
                    it[Transactions.date].date,
                    it[Transactions.amount].toDouble(),
                    it[Transactions.category]
                )
            }
        }

        // Calculate spending per day this week
        val spendingPerDayThisWeek = (0..6).map { offset ->
            val date = startOfWeek.plus(DatePeriod(days = offset))
            allTransactions
                .filter { it.first == date && it.second < 0 }
                .sumOf { -it.second }.toInt()
        }

        // Calculate income per day this week
        val incomePerDayThisWeek = (0..6).map { offset ->
            val date = startOfWeek.plus(DatePeriod(days = offset))
            allTransactions
                .filter { it.first == date && it.second > 0 }
                .sumOf { it.second }.toInt()
        }

        // Calculate transactions count per day this week
        val transactionsPerDayThisWeek = (0..6).map { offset ->
            val date = startOfWeek.plus(DatePeriod(days = offset))
            allTransactions.count { it.first == date }
        }

        // Calculate spending by category
        val spendingByCategory = allTransactions
            .filter { it.second < 0 && it.third.isNotEmpty() }
            .groupBy { it.third }
            .mapValues { (_, transactions) -> 
                transactions.sumOf { -it.second }
            }

        // Calculate monthly totals
        val monthlySpending = allTransactions
            .filter { it.second < 0 }
            .sumOf { -it.second }

        val monthlyIncome = allTransactions
            .filter { it.second > 0 }
            .sumOf { it.second }

        // Calculate savings rate
        val savingsRate = if (monthlyIncome > 0) {
            ((monthlyIncome - monthlySpending) / monthlyIncome * 100).coerceAtLeast(0.0)
        } else {
            0.0
        }

        // Get total balance
        val totalBalance = accountService.getTotalBalance(userId)

        // Calculate monthly trends (last 6 months)
        val monthlyTrends = calculateMonthlyTrends(userId, today)

        return FinanceStatisticsDTO(
            totalBalance = totalBalance,
            monthlyIncome = monthlyIncome,
            monthlySpending = monthlySpending,
            savingsRate = savingsRate,
            spendingPerDayThisWeek = spendingPerDayThisWeek,
            incomePerDayThisWeek = incomePerDayThisWeek,
            transactionsPerDayThisWeek = transactionsPerDayThisWeek,
            spendingByCategory = spendingByCategory,
            monthlyTrends = monthlyTrends
        )
    }

    private fun calculateMonthlyTrends(userId: Int, currentDate: LocalDate): MonthlyTrendDTO {
        val months = mutableListOf<String>()
        val income = mutableListOf<Double>()
        val spending = mutableListOf<Double>()

        // Get last 6 months
        for (i in 5 downTo 0) {
            val monthDate = currentDate.minus(DatePeriod(months = i))
            val monthStart = LocalDate(monthDate.year, monthDate.monthNumber, 1)
            val monthEnd = monthStart.plus(DatePeriod(months = 1)).minus(1, DateTimeUnit.DAY)
            
            months.add("${monthDate.month.name.take(3)} ${monthDate.year}")

            val monthTransactions = transaction {
                Transactions.selectAll().where {
                    (Transactions.user eq userId) and
                    (Transactions.status eq Status.ACTIVE) and
                    (Transactions.date.date() greaterEq monthStart) and
                    (Transactions.date.date() lessEq monthEnd)
                }.map {
                    it[Transactions.amount].toDouble()
                }
            }

            income.add(monthTransactions.filter { it > 0 }.sum())
            spending.add(monthTransactions.filter { it < 0 }.sumOf { -it })
        }

        return MonthlyTrendDTO(
            months = months,
            income = income,
            spending = spending
        )
    }
}

