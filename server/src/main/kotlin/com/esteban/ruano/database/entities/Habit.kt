package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import com.lifecommander.models.Frequency
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object Habits : UUIDTable() {
    val name = varchar("name", 50)
    val frequency = varchar("frequency", 50).default(Frequency.DAILY.value)
    val note = varchar("note", 255)
    val baseDateTime = datetime("base_date_time")
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Habit(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Habit>(Habits)

    var name by Habits.name
    var frequency by Habits.frequency
    var note by Habits.note
    var baseDateTime by Habits.baseDateTime
    var status by Habits.status
    var user by User referencedOn Habits.user
}