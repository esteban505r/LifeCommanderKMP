package com.esteban.ruano.workout_data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val description: String,
    val restSecs: Int,
    val baseSets: Int,
    val baseReps: Int,
    val muscleGroup: String,
    val resourceId: Int? = null,
    val remoteId: Int? = null,
)