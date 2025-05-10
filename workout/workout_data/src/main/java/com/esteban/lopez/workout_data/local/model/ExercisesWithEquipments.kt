package com.esteban.ruano.workout_data.local.model

import androidx.room.Entity

@Entity(primaryKeys = ["exerciseId", "equipmentId"], tableName = ExercisesWithEquipments.TABLE_NAME)
data class ExercisesWithEquipments(
    val exerciseId: Int,
    val equipmentId: Int
){
    companion object{
        const val TABLE_NAME = "exercises_with_equipments"
    }
}