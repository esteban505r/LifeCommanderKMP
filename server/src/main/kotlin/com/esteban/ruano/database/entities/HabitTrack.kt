package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object HabitTracks : IntIdTable(){

    val habitId = reference("habit_id", Habits.id, onDelete = ReferenceOption.CASCADE)
    val doneDateTime = datetime("done_date_time")
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class HabitTrack(id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<HabitTrack>(HabitTracks)

    var date by HabitTracks.doneDateTime
    var habit by Habit referencedOn HabitTracks.habitId
    var doneDateTime by HabitTracks.doneDateTime
    var status by HabitTracks.status
}