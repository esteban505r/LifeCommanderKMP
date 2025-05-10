package com.esteban.ruano.habits_data.datasources

import com.esteban.ruano.habits_domain.model.Habit

interface HabitsDataSource {

    suspend fun getHabits(
        filter: String,
        page: Int,
        limit: Int,
        date: String? = null
    ): List<Habit>

    suspend fun getHabitsByDateRange(
        filter: String,
        page: Int,
        limit: Int,
        startDate: String? = null,
        endDate: String? = null
    ): List<Habit>

    suspend fun getHabitsNoDueDate(
        filter: String,
        page: Int,
        limit: Int
    ): List<Habit>

    suspend fun getHabit(id: String, date: String? = null): Habit

    suspend fun addHabit(habit: Habit): Unit

    suspend fun deleteHabit(id: String)

    suspend fun completeHabit(id: String, datetime: String)

    suspend fun unCompleteHabit(id: String, datetime: String)

    suspend fun updateHabit(id: String, habit: Habit)
}
