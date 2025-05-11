package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date
import java.util.UUID

object DailyJournals : UUIDTable() {
    val date = date("date")
    val summary = text("summary")
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class DailyJournal(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DailyJournal>(DailyJournals)
    var date by DailyJournals.date
    var summary by DailyJournals.summary
    var user by User referencedOn DailyJournals.user
    var status by DailyJournals.status
} 