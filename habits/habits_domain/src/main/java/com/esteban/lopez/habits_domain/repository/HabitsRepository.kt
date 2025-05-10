package com.esteban.ruano.habits_domain.repository

import com.esteban.ruano.habits_domain.model.Habit

interface HabitsRepository {
    suspend fun getHabits(
        filter: String?,
        page: Int?,
        limit: Int?,
        date: String
    ): Result<List<Habit>>

    suspend fun getHabit(habitId: String, date: String): Result<Habit>

    suspend fun addHabit(habit: Habit): Result<Unit>

    suspend fun deleteHabit(habitId: String): Result<Unit>

    suspend fun completeHabit(habitId: String): Result<Unit>

    suspend fun unCompleteHabit(habitId: String): Result<Unit>

    suspend fun updateHabit(id:String,habit: Habit): Result<Unit>

    suspend fun getHabitsByDateRange(
        filter: String?,
        page: Int?,
        limit: Int?,
        startDate: String?,
        endDate: String?
    ): Result<List<Habit>>

}