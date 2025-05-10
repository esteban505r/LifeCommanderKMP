package com.esteban.ruano.tasks_data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = Task.TABLE_NAME)
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val dueDate: String? = null,
    val scheduledDate: String? = null,
    val completed: Boolean = false,
    val createdAt: String = System.currentTimeMillis().toString(),
    val updatedAt: String? = null,
    val priority: Int = 0,
    val completedAt: String? = null,
    val remoteId: Int? = null,
){
    companion object{
        const val TABLE_NAME = "tasks"
    }
}