package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID

object Questions : UUIDTable() {
    val question = varchar("question", 255)
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Question(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Question>(Questions)
    var question by Questions.question
    var user by User referencedOn Questions.user
    var status by Questions.status
} 