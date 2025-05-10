package com.esteban.ruano.workout_data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = Resource.TABLE_NAME)
data class Resource(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val url: String,
    val type: String,
    val remoteId: Int? = null,
){
    companion object{
        const val TABLE_NAME = "resources"
    }
}
