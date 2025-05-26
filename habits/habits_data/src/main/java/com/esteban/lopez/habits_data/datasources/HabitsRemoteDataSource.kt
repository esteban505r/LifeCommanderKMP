package com.esteban.ruano.habits_data.datasources

import com.esteban.ruano.habits_data.mapper.toDomainModel
import com.esteban.ruano.habits_data.mapper.toResponseModel
import com.esteban.ruano.habits_data.remote.HabitsApi
import com.lifecommander.models.Habit

class HabitsRemoteDataSource(
    private val habitsApi: HabitsApi
) : HabitsDataSource {

    override suspend fun getHabits(
        filter: String,
        page: Int,
        limit: Int,
        date: String?
    ): List<Habit> {
        val habitResponses = habitsApi.getHabits(filter, page, limit, date)
        return habitResponses.map { it.toDomainModel() }
    }

    override suspend fun getHabitsByDateRange(
        filter: String,
        page: Int,
        limit: Int,
        startDate: String?,
        endDate: String?
    ): List<Habit> {
        val habitResponses = habitsApi.getHabitsByDateRange(filter, page, limit, startDate, endDate)
        return habitResponses.map { it.toDomainModel() }
    }

    override suspend fun getHabitsNoDueDate(
        filter: String,
        page: Int,
        limit: Int
    ): List<Habit> {
        val habitResponses = habitsApi.getHabitsNoDueDate(filter, page, limit)
        return habitResponses.map { it.toDomainModel() }
    }

    override suspend fun getHabit(id: String, date: String?): Habit {
        val habitResponse = habitsApi.getHabit(id, date ?: "")
        return habitResponse.toDomainModel()
    }

    override suspend fun addHabit(habit: Habit) {
        val habitResponse = habit.toResponseModel()
        return habitsApi.addHabit(habitResponse)
    }

    override suspend fun deleteHabit(id: String) {
        habitsApi.deleteHabit(id)
    }

    override suspend fun completeHabit(id: String, datetime: String) {
        habitsApi.completeHabit(id, datetime)
    }

    override suspend fun unCompleteHabit(id: String, datetime: String) {
        habitsApi.unCompleteHabit(id, datetime)
    }

    override suspend fun updateHabit(id: String, habit: Habit) {
        val habitResponse = habit.toResponseModel()
        habitsApi.updateHabit(id, habitResponse)
    }
}
