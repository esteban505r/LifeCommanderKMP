package services.habits

import services.habits.models.HabitRequest
import services.habits.models.HabitResponse

interface HabitRepository {
    suspend fun getByDate(token: String, page: Int, limit: Int, date: String): List<HabitResponse>
    suspend fun completeHabit(token: String, id: String, dateTime: String)
    suspend fun unCompleteHabit(token: String, id: String, dateTime: String)
    suspend fun addHabit(token: String, name: String, note: String?, frequency: String, dateTime: String)
    suspend fun updateHabit(token: String, id: String, habit: HabitResponse)
    suspend fun deleteHabit(token: String, id: String)
} 