package com.lifecommander.finance.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime

@Serializable
data class SavingsGoal(
    val id: String? = null,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: LocalDateTime?,
    val accountId: String,
    val isCompleted: Boolean = false,
    val monthlyContribution: Double = 0.0
)

@Serializable
data class SavingsGoalProgress(
    val goal: SavingsGoal,
    val percentageComplete: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val estimatedCompletionDate: LocalDateTime? = null,
    val monthlyContributionNeeded: Double = 0.0
) 