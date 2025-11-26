package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

enum class StudyItemStage {
    PENDING,
    IN_PROGRESS,
    PROCESSED
}

enum class StudyMode {
    INPUT,
    PROCESSING,
    REVIEW
}

object StudyItems : UUIDTable() {
    val topicId = reference("topic_id", StudyTopics.id, ReferenceOption.CASCADE).nullable()
    val title = varchar("title", 200)
    val obsidianPath = varchar("obsidian_path", 500).nullable()
    val stage = enumerationByName("stage", 20, StudyItemStage::class).default(StudyItemStage.PENDING)
    val modeHint = enumerationByName("mode_hint", 20, StudyMode::class).nullable()
    val discipline = varchar("discipline", 50).nullable()
    val progress = integer("progress").default(0) // 0-100
    val estimatedEffortMinutes = integer("estimated_effort_minutes").nullable()
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class StudyItem(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<StudyItem>(StudyItems)

    var topic by StudyTopic optionalReferencedOn StudyItems.topicId // Expose topic object, not topicId
    var title by StudyItems.title
    var obsidianPath by StudyItems.obsidianPath
    var stage by StudyItems.stage
    var modeHint by StudyItems.modeHint
    var discipline by StudyItems.discipline
    var progress by StudyItems.progress
    var estimatedEffortMinutes by StudyItems.estimatedEffortMinutes
    var user by User referencedOn StudyItems.user
    var status by StudyItems.status
    var createdAt by StudyItems.createdAt
    var updatedAt by StudyItems.updatedAt
}

