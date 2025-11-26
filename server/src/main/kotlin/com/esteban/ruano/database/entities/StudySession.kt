package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

object StudySessions : UUIDTable() {
    val topicId = reference("topic_id", StudyTopics.id, ReferenceOption.CASCADE).nullable()
    val studyItemId = reference("study_item_id", StudyItems.id, ReferenceOption.SET_NULL).nullable()
    val mode = enumerationByName("mode", 20, StudyMode::class)
    val plannedStart = datetime("planned_start").nullable()
    val plannedEnd = datetime("planned_end").nullable()
    val actualStart = datetime("actual_start").nullable()
    val actualEnd = datetime("actual_end").nullable()
    val durationMinutes = integer("duration_minutes").nullable()
    val notes = text("notes").nullable()
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class StudySession(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<StudySession>(StudySessions)

    var topic by StudyTopic optionalReferencedOn StudySessions.topicId // Expose topic object, not topicId
    var studyItem by StudyItem optionalReferencedOn StudySessions.studyItemId // Expose studyItem object, not studyItemId
    var mode by StudySessions.mode
    var plannedStart by StudySessions.plannedStart
    var plannedEnd by StudySessions.plannedEnd
    var actualStart by StudySessions.actualStart
    var actualEnd by StudySessions.actualEnd
    var durationMinutes by StudySessions.durationMinutes
    var notes by StudySessions.notes
    var user by User referencedOn StudySessions.user
    var status by StudySessions.status
    var createdAt by StudySessions.createdAt
    var updatedAt by StudySessions.updatedAt
}

