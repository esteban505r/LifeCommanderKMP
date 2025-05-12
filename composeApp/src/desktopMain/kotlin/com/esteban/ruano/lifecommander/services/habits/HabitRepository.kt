package services.habits

import com.esteban.ruano.models.Habit

interface HabitRepository {
    suspend fun getByDate(token: String, page: Int, limit: Int, date: String): List<Habit>
    suspend fun getByDateRange(token: String, page: Int, limit: Int, startDate: String, endDate: String, excludeDaily: Boolean = true): List<Habit>
    suspend fun completeHabit(token: String, id: String, dateTime: String)
    suspend fun unCompleteHabit(token: String, id: String, dateTime: String)
    suspend fun addHabit(token: String, name: String, note: String?, frequency: String, dateTime: String)
    suspend fun updateHabit(token: String, id: String, habit: Habit)
    suspend fun deleteHabit(token: String, id: String)
} 