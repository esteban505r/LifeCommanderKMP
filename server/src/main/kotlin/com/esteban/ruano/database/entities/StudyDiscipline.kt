package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

object StudyDisciplines : UUIDTable() {
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val color = varchar("color", 20).nullable()
    val iconUrl = varchar("icon_url", 500).nullable() // S3 URL for icon image
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class StudyDiscipline(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<StudyDiscipline>(StudyDisciplines)

    var name by StudyDisciplines.name
    var description by StudyDisciplines.description
    var color by StudyDisciplines.color
    var iconUrl by StudyDisciplines.iconUrl
    var user by User referencedOn StudyDisciplines.user
    var status by StudyDisciplines.status
    var createdAt by StudyDisciplines.createdAt
    var updatedAt by StudyDisciplines.updatedAt
}

