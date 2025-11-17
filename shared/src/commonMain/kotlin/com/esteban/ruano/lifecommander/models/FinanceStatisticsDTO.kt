package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class FinanceStatisticsDTO(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlySpending: Double = 0.0,
    val savingsRate: Double = 0.0,
    val spendingPerDayThisWeek: List<Int> = emptyList(),
    val incomePerDayThisWeek: List<Int> = emptyList(),
    val transactionsPerDayThisWeek: List<Int> = emptyList(),
    val spendingByCategory: Map<String, Double> = emptyMap(),
    val monthlyTrends: MonthlyTrendDTO? = null
)

@Serializable
data class MonthlyTrendDTO(
    val months: List<String> = emptyList(),
    val income: List<Double> = emptyList(),
    val spending: List<Double> = emptyList()
)

