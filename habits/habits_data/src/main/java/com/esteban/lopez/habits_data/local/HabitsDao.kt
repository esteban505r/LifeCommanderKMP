package com.esteban.ruano.habits_data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.esteban.ruano.habits_data.local.model.Habit

@Dao
interface HabitsDao {

    @Query("SELECT * FROM habits WHERE name LIKE '%' || :filter || '%' ORDER BY id LIMIT :limit OFFSET :page * :limit")
    suspend fun getHabits(
        filter: String,
        page: Int? = 0,
        limit: Int? = 20
    ): List<Habit>

    @Query("SELECT * FROM habits WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime LIMIT :limit OFFSET :page * :limit")
    suspend fun getHabitsByDateRange(
        startDate: String?,
        endDate: String?,
        page: Int? = 0,
        limit: Int? = 20
    ): List<Habit>

    @Query("SELECT * FROM habits WHERE dateTime IS NULL OR dateTime = '' ORDER BY id LIMIT :limit OFFSET :page * :limit")
    suspend fun getHabitsNoDueDate(
        page: Int? = 0,
        limit: Int? = 20
    ): List<Habit>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabit(id: String): Habit

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: String)

    @Query("UPDATE habits SET done = 1, dateTime = :datetime WHERE id = :id")
    suspend fun completeHabit(id: String, datetime: String)

    @Query("UPDATE habits SET done = 0, dateTime = :datetime WHERE id = :id")
    suspend fun unCompleteHabit(id: String, datetime: String)

    @Update
    suspend fun updateHabit(habit: Habit)
}
