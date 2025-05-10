package com.esteban.ruano.workout_data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = Equipment.TABLE_NAME)
data class Equipment(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val description: String,
    val resourceId: Int? = null,
    val remoteId: Int? = null,
){
    companion object{
        const val TABLE_NAME = "equipments"
    }
}
