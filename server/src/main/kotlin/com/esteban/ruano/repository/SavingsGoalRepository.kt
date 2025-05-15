package com.esteban.ruano.repository

import com.esteban.ruano.service.SavingsGoalService
import com.esteban.ruano.database.entities.SavingsGoal
import com.esteban.ruano.models.finance.SavingsGoalResponseDTO
import kotlinx.datetime.LocalDate
import java.util.*

class SavingsGoalRepository(private val service: SavingsGoalService) {
    fun create(userId: Int, name: String, targetAmount: Double, targetDate: LocalDate): UUID? =
        service.createSavingsGoal(userId, name, targetAmount, targetDate)

    fun getAll(userId: Int): List<SavingsGoalResponseDTO> = service.getSavingsGoalsByUser(userId)

    fun update(userId: Int, goalId: UUID, name: String?, targetAmount: Double?, targetDate: LocalDate?): Boolean =
        service.updateSavingsGoal(goalId, userId, name, targetAmount, targetDate)

    fun delete(userId: Int, goalId: UUID): Boolean = service.deleteSavingsGoal(goalId, userId)

    fun updateProgress(userId: Int, goalId: UUID, amount: Double): Boolean = service.updateProgress( userId,goalId, amount)

    fun getProgress(userId: Int, goalId: UUID): Double = service.getProgress(userId, goalId)

    fun getRemainingAmount(userId: Int, goalId: UUID): Double = service.getRemainingAmount(userId, goalId)
} 