package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.date
import java.util.*

object DailyJournals : UUIDTable() {
    val date = date("date")
    val summary = text("summary")
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
    
    init {
        uniqueIndex(user, date) // Ensure one journal entry per user per date
    }
}

class DailyJournal(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DailyJournal>(DailyJournals)
    var date by DailyJournals.date
    var summary by DailyJournals.summary
    var user by User referencedOn DailyJournals.user
    var status by DailyJournals.status
} 