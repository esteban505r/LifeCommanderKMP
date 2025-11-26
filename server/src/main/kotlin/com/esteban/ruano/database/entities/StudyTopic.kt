package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.*

object StudyTopics : UUIDTable() {
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val disciplineId = reference("discipline_id", StudyDisciplines.id, ReferenceOption.SET_NULL).nullable()
    val color = varchar("color", 20).nullable()
    val iconUrl = varchar("icon_url", 500).nullable() // S3 URL for icon image
    val isActive = bool("is_active").default(true)
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class StudyTopic(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<StudyTopic>(StudyTopics)

    var name by StudyTopics.name
    var description by StudyTopics.description
    var discipline by StudyDiscipline optionalReferencedOn StudyTopics.disciplineId // Expose discipline object, not disciplineId
    var color by StudyTopics.color
    var iconUrl by StudyTopics.iconUrl
    var isActive by StudyTopics.isActive
    var status by StudyTopics.status
    var user by User referencedOn StudyTopics.user
}

