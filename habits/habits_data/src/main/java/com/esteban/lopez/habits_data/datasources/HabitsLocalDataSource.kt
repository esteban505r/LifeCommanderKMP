package com.esteban.ruano.habits_data.datasources
import com.esteban.ruano.core_data.local.HistoryTrackDao
import com.esteban.ruano.core_data.local.addDeleteOperation
import com.esteban.ruano.core_data.local.addInsertOperation
import com.esteban.ruano.core_data.local.addUpdateOperation
import com.esteban.ruano.habits_data.local.HabitsDao
import com.esteban.ruano.habits_data.mapper.toDatabaseEntity
import com.esteban.ruano.habits_data.mapper.toDomainModel
import com.esteban.ruano.habits_domain.model.Habit
import com.esteban.ruano.habits_data.local.model.Habit as LocalHabit

class HabitsLocalDataSource(
    private val habitsDao: HabitsDao,
    private val historyTrackDao: HistoryTrackDao
) : HabitsDataSource {

    override suspend fun getHabits(
        filter: String,
        page: Int,
        limit: Int,
        date: String?
    ): List<Habit> {
        val habits = habitsDao.getHabits(filter, page, limit)
        return habits.map { it.toDomainModel() }
    }

    override suspend fun getHabitsByDateRange(
        filter: String,
        page: Int,
        limit: Int,
        startDate: String?,
        endDate: String?
    ): List<Habit> {
        val habits = habitsDao.getHabitsByDateRange(startDate, endDate, page, limit)
        return habits.map { it.toDomainModel() }
    }

    override suspend fun getHabitsNoDueDate(
        filter: String,
        page: Int,
        limit: Int
    ): List<Habit> {
        val habits = habitsDao.getHabitsNoDueDate(page, limit )
        return habits.map { it.toDomainModel() }
    }

    override suspend fun getHabit(id: String, date: String?): Habit {
        val habitEntity = habitsDao.getHabit(id)
        return habitEntity.toDomainModel()
    }

    override suspend fun addHabit(habit: Habit) {
        val habitEntity = habit.toDatabaseEntity()
        val id = habitsDao.addHabit(habitEntity)
        historyTrackDao.addInsertOperation(LocalHabit.TABLE_NAME, habit.id)
    }

    override suspend fun deleteHabit(id: String) {
        habitsDao.deleteHabit(id)
        historyTrackDao.addDeleteOperation(LocalHabit.TABLE_NAME, id)
    }

    override suspend fun completeHabit(id: String, datetime: String) {
        habitsDao.completeHabit(id, datetime)
        historyTrackDao.addUpdateOperation(LocalHabit.TABLE_NAME, id)
    }

    override suspend fun unCompleteHabit(id: String, datetime: String) {
        habitsDao.unCompleteHabit(id, datetime)
        historyTrackDao.addUpdateOperation(LocalHabit.TABLE_NAME, id)
    }

    override suspend fun updateHabit(id: String, habit: Habit) {
        val habitEntity = habit.toDatabaseEntity()
        habitsDao.updateHabit(habitEntity)
        historyTrackDao.addUpdateOperation(LocalHabit.TABLE_NAME, id)
    }
}
