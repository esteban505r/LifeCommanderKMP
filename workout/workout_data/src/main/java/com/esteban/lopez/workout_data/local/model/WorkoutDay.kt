package com.esteban.ruano.workout_data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = WorkoutDay.TABLE_NAME)
data class WorkoutDay(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val day: Int,
    val name: String,
    val time: String,
    val remoteId: Int? = null,
){
    companion object{
        const val TABLE_NAME = "workout_days"
    }
}