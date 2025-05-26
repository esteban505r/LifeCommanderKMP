package com.esteban.ruano.habits_data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifecommander.models.Frequency
import java.util.UUID

@Entity(tableName = Habit.TABLE_NAME)
data class Habit(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dateTime: String,
    val frequency: String = Frequency.DAILY.value,
    val note: String = "",
    val createdAt: String = System.currentTimeMillis().toString(),
    val updatedAt: String? = null,
    val done: Boolean = false,
    val remoteId: Int? = null,
){
    companion object{
        const val TABLE_NAME = "habits"
    }
}