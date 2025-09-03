package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.finance.*
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.jdbc.and
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

class SavingsGoalService : BaseService() {
    fun createSavingsGoal(
        userId: Int,
        name: String,
        targetAmount: Double,
        targetDate: LocalDate
    ): UUID? {
        return transaction {
            SavingsGoals.insertOperation(userId) {
                insert {
                    it[this.name] = name
                    it[this.targetAmount] = targetAmount.toBigDecimal()
                    it[this.currentAmount] = 0.toBigDecimal()
                    it[this.targetDate] = targetDate
                    it[this.user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
        }
    }

    fun getSavingsGoalsByUser(userId: Int): List<SavingsGoalResponseDTO> {
        return transaction {
            SavingsGoal.find { SavingsGoals.user eq userId }
                .map { it.toResponseDTO() }
        }
    }

    fun getProgress(userId: Int, goalId: UUID): Double {
        return transaction {
            val goal = SavingsGoal.findById(goalId)
            if (goal != null && goal.user.id.value == userId) {
                goal.currentAmount.toDouble() / goal.targetAmount.toDouble()
            } else {
                0.0
            }
        }
    }

    fun getRemainingAmount(userId: Int, goalId: UUID): Double {
        return transaction {
            val goal = SavingsGoal.findById(goalId)
            if (goal != null && goal.user.id.value == userId) {
                goal.targetAmount.toDouble() - goal.currentAmount.toDouble()
            } else {
                0.0
            }
        }
    }

    fun updateSavingsGoal(
        goalId: UUID,
        userId: Int,
        name: String? = null,
        targetAmount: Double? = null,
        targetDate: LocalDate? = null
    ): Boolean {
        return transaction {
            val goal = SavingsGoal.findById(goalId)
            if (goal != null && goal.user.id.value == userId) {
                name?.let { goal.name = it }
                targetAmount?.let { goal.targetAmount = it.toBigDecimal() }
                targetDate?.let { goal.targetDate = it }
                true
            } else {
                false
            }
        }
    }

    fun deleteSavingsGoal(goalId: UUID, userId: Int): Boolean {
        return transaction {
            val goal = SavingsGoal.findById(goalId)
            if (goal != null && goal.user.id.value == userId) {
                goal.status = Status.INACTIVE
                true
            } else {
                false
            }
        }
    }

    fun updateProgress(userId: Int, goalId: UUID, amount: Double): Boolean {
        return transaction {
            val goal = SavingsGoal.findById(goalId)
            if (goal != null && goal.user.id.value == userId) {
                goal.currentAmount = (goal.currentAmount.toDouble() + amount).toBigDecimal()
                true
            } else {
                false
            }
        }
    }
} 