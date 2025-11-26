package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toStudyItemDTO
import com.esteban.ruano.database.converters.toStudySessionDTO
import com.esteban.ruano.database.converters.toStudyTopicDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.study.*
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.parseDate
import com.esteban.ruano.utils.parseDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class StudyService : BaseService() {

    // StudyTopic operations
    fun createTopic(userId: Int, dto: CreateStudyTopicDTO): UUID? {
        return transaction {
            val disciplineId = dto.disciplineId?.let { UUID.fromString(it) }
            val insertedRow = StudyTopics.insertOperation(userId) {
                insert {
                    it[name] = dto.name
                    it[this.description] = dto.description
                    it[this.disciplineId] = disciplineId
                    it[color] = dto.color
                    it[this.iconUrl] = dto.icon
                    it[isActive] = dto.isActive
                    it[user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
            insertedRow
        }
    }

    fun updateTopic(userId: Int, id: UUID, dto: UpdateStudyTopicDTO): Boolean {
        return transaction {
            val disciplineId = dto.disciplineId?.let { UUID.fromString(it) }
            val updatedRow = StudyTopics.updateOperation(userId) {
                val updatedRows = update({ (StudyTopics.id eq id) }) { row ->
                    dto.name?.let { row[name] = it }
                    dto.description?.let { row[description] = it }
                    dto.disciplineId?.let { row[StudyTopics.disciplineId] = disciplineId }
                    dto.color?.let { row[color] = it }
                    dto.icon?.let { row[iconUrl] = it }
                    dto.isActive?.let { row[isActive] = it }
                }
                if (updatedRows > 0) id else null
            }
            updatedRow != null
        }
    }

    fun deleteTopic(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = StudyTopics.deleteOperation(userId) {
                val updatedRows = StudyTopics.update({ (StudyTopics.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }

    fun getAllTopics(userId: Int, isActive: Boolean? = null): List<StudyTopicDTO> {
        return transaction {
            val query = if (isActive != null) {
                (StudyTopics.user eq userId) and (StudyTopics.status eq Status.ACTIVE) and (StudyTopics.isActive eq isActive)
            } else {
                (StudyTopics.user eq userId) and (StudyTopics.status eq Status.ACTIVE)
            }
            StudyTopic.find(query)
                .with(StudyTopic::discipline) // Eagerly load discipline relationship
                .orderBy(StudyTopics.name to SortOrder.ASC)
                .map { it.toStudyTopicDTO() }
        }
    }

    fun getTopicById(userId: Int, id: UUID): StudyTopicDTO? {
        return transaction {
            StudyTopic.find {
                (StudyTopics.user eq userId) and (StudyTopics.id eq id) and (StudyTopics.status eq Status.ACTIVE)
            }
                .with(StudyTopic::discipline) // Eagerly load discipline relationship
                .firstOrNull()?.toStudyTopicDTO()
        }
    }

    // StudyItem operations
    fun createItem(userId: Int, dto: CreateStudyItemDTO): UUID? {
        return transaction {
            val topicId = dto.topicId?.let { UUID.fromString(it) }
            val modeHint = dto.modeHint?.let { StudyMode.valueOf(it) }
            val stage = StudyItemStage.valueOf(dto.stage)
            val now = getCurrentDateTime(TimeZone.currentSystemDefault())
            
            val insertedRow = StudyItems.insertOperation(userId) {
                insert {
                    it[this.topicId] = topicId
                    it[title] = dto.title
                    it[obsidianPath] = dto.obsidianPath
                    it[this.stage] = stage
                    it[this.modeHint] = modeHint
                    it[discipline] = dto.discipline
                    it[progress] = dto.progress.coerceIn(0, 100)
                    it[estimatedEffortMinutes] = dto.estimatedEffortMinutes
                    it[user] = userId
                    it[createdAt] = now
                    it[updatedAt] = now
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
            insertedRow
        }
    }

    fun updateItem(userId: Int, id: UUID, dto: UpdateStudyItemDTO): Boolean {
        return transaction {
            val updatedRow = StudyItems.updateOperation(userId) {
                val updatedRows = update({ (StudyItems.id eq id) }) { row ->
                    dto.topicId?.let { row[topicId] = UUID.fromString(it) }
                    dto.title?.let { row[title] = it }
                    dto.obsidianPath?.let { row[obsidianPath] = it }
                    dto.stage?.let { row[stage] = StudyItemStage.valueOf(it) }
                    dto.modeHint?.let { row[modeHint] = StudyMode.valueOf(it) }
                    dto.discipline?.let { row[discipline] = it }
                    dto.progress?.let { row[progress] = it.coerceIn(0, 100) }
                    dto.estimatedEffortMinutes?.let { row[estimatedEffortMinutes] = it }
                    row[updatedAt] = getCurrentDateTime(TimeZone.currentSystemDefault())
                }
                if (updatedRows > 0) id else null
            }
            updatedRow != null
        }
    }

    fun deleteItem(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = StudyItems.deleteOperation(userId) {
                val updatedRows = StudyItems.update({ (StudyItems.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }

    fun getAllItems(
        userId: Int,
        topicId: UUID? = null,
        stage: String? = null,
        search: String? = null
    ): List<StudyItemDTO> {
        return transaction {
            var query = (StudyItems.user eq userId) and (StudyItems.status eq Status.ACTIVE)
            
            topicId?.let { query = query and (StudyItems.topicId eq it) }
            stage?.let { query = query and (StudyItems.stage eq StudyItemStage.valueOf(it)) }
            search?.let { 
                query = query and (StudyItems.title like "%${it}%")
            }
            
            StudyItem.find(query)
                .with(StudyItem::topic) // Eagerly load topic relationship
                .orderBy(StudyItems.createdAt to SortOrder.DESC)
                .map { it.toStudyItemDTO() }
        }
    }

    fun getItemById(userId: Int, id: UUID): StudyItemDTO? {
        return transaction {
            StudyItem.find {
                (StudyItems.user eq userId) and (StudyItems.id eq id) and (StudyItems.status eq Status.ACTIVE)
            }
                .with(StudyItem::topic) // Eagerly load topic relationship
                .firstOrNull()?.toStudyItemDTO()
        }
    }

    // StudySession operations
    fun createSession(userId: Int, dto: CreateStudySessionDTO): UUID? {
        return transaction {
            val topicId = dto.topicId?.let { UUID.fromString(it) }
            val studyItemId = dto.studyItemId?.let { UUID.fromString(it) }
            val mode = StudyMode.valueOf(dto.mode)
            val now = getCurrentDateTime(TimeZone.currentSystemDefault())
            
            val insertedRow = StudySessions.insertOperation(userId) {
                insert {
                    it[this.topicId] = topicId
                    it[this.studyItemId] = studyItemId
                    it[this.mode] = mode
                    it[plannedStart] = dto.plannedStart?.let { parseDateTime(it) }
                    it[plannedEnd] = dto.plannedEnd?.let { parseDateTime(it) }
                    it[actualStart] = dto.actualStart?.let { parseDateTime(it) } ?: now
                    it[notes] = dto.notes
                    it[user] = userId
                    it[createdAt] = now
                    it[updatedAt] = now
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
            insertedRow
        }
    }

    @OptIn(ExperimentalTime::class)
    fun updateSession(userId: Int, id: UUID, dto: UpdateStudySessionDTO): Boolean {
        return transaction {
            val session = StudySession.findById(id) ?: return@transaction false
            if (session.user.id.value != userId) return@transaction false
            
            // Calculate duration if actualEnd is being set
            val durationMinutes = dto.actualEnd?.let { endTimeStr ->
                val endTime = parseDateTime(endTimeStr)
                // Use DTO value if provided, otherwise use existing session value
                val startTime = dto.actualStart?.let { parseDateTime(it) }
                    ?: session.actualStart
                    ?: dto.plannedStart?.let { parseDateTime(it) }
                    ?: session.plannedStart
                
                if (startTime != null) {
                    val tz = TimeZone.currentSystemDefault()
                    val startInstant: Instant = startTime.toInstant(tz)
                    val endInstant: Instant = endTime.toInstant(tz)
                    val duration = endInstant - startInstant
                    (duration.inWholeSeconds / 60).toInt()
                } else null
            } ?: dto.durationMinutes
            
            val updatedRows = StudySessions.update({ (StudySessions.id eq id) }) { row ->
                dto.topicId?.let { row[topicId] = UUID.fromString(it) }
                dto.studyItemId?.let { row[studyItemId] = UUID.fromString(it) }
                dto.mode?.let { row[mode] = StudyMode.valueOf(it) }
                dto.plannedStart?.let { row[plannedStart] = parseDateTime(it) }
                dto.plannedEnd?.let { row[plannedEnd] = parseDateTime(it) }
                dto.actualStart?.let { row[actualStart] = parseDateTime(it) }
                dto.actualEnd?.let { row[actualEnd] = parseDateTime(it) }
                durationMinutes?.let { row[this.durationMinutes] = it }
                dto.notes?.let { row[notes] = it }
                row[updatedAt] = getCurrentDateTime(TimeZone.currentSystemDefault())
            }
            updatedRows > 0
        }
    }

    @OptIn(ExperimentalTime::class)
    fun completeSession(userId: Int, id: UUID, actualEnd: String, notes: String? = null): Boolean {
        return transaction {
            val session = StudySession.findById(id) ?: return@transaction false
            if (session.user.id.value != userId) return@transaction false
            
            val endTime = parseDateTime(actualEnd)
            val startTime = session.actualStart ?: session.plannedStart
            
            val durationMinutes = if (startTime != null) {
                val tz = TimeZone.currentSystemDefault()
                val startInstant: Instant = startTime.toInstant(tz)
                val endInstant: Instant = endTime.toInstant(tz)
                val duration = endInstant - startInstant
                (duration.inWholeSeconds / 60).toInt()
            } else null
            
            val updatedRows = StudySessions.update({ (StudySessions.id eq id) }) { row ->
                row[this.actualEnd] = endTime
                row[this.durationMinutes] = durationMinutes
                notes?.let { row[this.notes] = it }
                row[updatedAt] = getCurrentDateTime(TimeZone.currentSystemDefault())
            }
            
            // Update linked StudyItem if processing mode and session is complete
            if (session.mode == StudyMode.PROCESSING && session.studyItem != null) {
                val item = session.studyItem
                if (item != null) {
                    StudyItems.update({ (StudyItems.id eq item.id) }) { row ->
                        row[stage] = StudyItemStage.PROCESSED
                        row[progress] = 100
                        row[updatedAt] = getCurrentDateTime(TimeZone.currentSystemDefault())
                    }
                }
            }
            
            updatedRows > 0
        }
    }

    fun deleteSession(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = StudySessions.deleteOperation(userId) {
                val updatedRows = StudySessions.update({ (StudySessions.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getAllSessions(
        userId: Int,
        topicId: UUID? = null,
        mode: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): List<StudySessionDTO> {
        return transaction {
            var query = (StudySessions.user eq userId) and (StudySessions.status eq Status.ACTIVE)
            
            topicId?.let { query = query and (StudySessions.topicId eq it) }
            mode?.let { query = query and (StudySessions.mode eq StudyMode.valueOf(it)) }
            
            if (startDate != null && endDate != null) {
                val start = parseDate(startDate)
                val end = parseDate(endDate)
                val tz = TimeZone.currentSystemDefault()
                val startDateTime = start.atStartOfDayIn(tz)
                val endDateTime = end.plus(DatePeriod(days = 1)).atStartOfDayIn(tz)
                query = query and (StudySessions.createdAt.greaterEq(startDateTime.toLocalDateTime(tz)))
                    .and (StudySessions.createdAt.lessEq(endDateTime.toLocalDateTime(tz)))
            }
            
            StudySession.find(query)
                .orderBy(StudySessions.createdAt to SortOrder.DESC)
                .map { it.toStudySessionDTO() }
        }
    }

    fun getSessionById(userId: Int, id: UUID): StudySessionDTO? {
        return transaction {
            StudySession.find {
                (StudySessions.user eq userId) and (StudySessions.id eq id) and (StudySessions.status eq Status.ACTIVE)
            }
                .with(StudySession::topic, StudySession::studyItem) // Eagerly load relationships
                .firstOrNull()?.toStudySessionDTO()
        }
    }

    fun getAllDisciplines(userId: Int): List<StudyDiscipline> {
        return transaction {
            StudyTopic.find {
                (StudyTopics.user eq userId) and
                (StudyTopics.status eq Status.ACTIVE) and
                (StudyTopics.disciplineId.isNotNull())
            }
                .mapNotNull { it.discipline }
                .distinct()
                .sortedBy { it.name }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getStats(userId: Int, startDate: String? = null, endDate: String? = null): StudyStatsDTO {
        return transaction {
            var sessionQuery = (StudySessions.user eq userId) and (StudySessions.status eq Status.ACTIVE)
            
            if (startDate != null && endDate != null) {
                val start = parseDate(startDate)
                val end = parseDate(endDate)
                val tz = TimeZone.currentSystemDefault()
                val startDateTime = start.atStartOfDayIn(tz)
                val endDateTime = end.plus(DatePeriod(days = 1)).atStartOfDayIn(tz)
                sessionQuery = sessionQuery and (StudySessions.createdAt.greaterEq(startDateTime.toLocalDateTime(tz)))
                    .and (StudySessions.createdAt.lessEq(endDateTime.toLocalDateTime(tz)))
            }
            
            val sessions = StudySession.find(sessionQuery).toList()
            
            val totalTimeByTopic = sessions
                .filter { it.topic != null && it.durationMinutes != null }
                .groupBy { it.topic!!.id.value.toString() }
                .mapValues { (_, sessions) -> sessions.sumOf { it.durationMinutes ?: 0 } }
            
            val totalTimeByMode = sessions
                .filter { it.durationMinutes != null }
                .groupBy { it.mode.name }
                .mapValues { (_, sessions) -> sessions.sumOf { it.durationMinutes ?: 0 } }
            
            var itemQuery = (StudyItems.user eq userId) and (StudyItems.status eq Status.ACTIVE)
            val items = StudyItem.find(itemQuery).toList()
            
            val itemsByStage = items
                .groupBy { it.stage.name }
                .mapValues { (_, items) -> items.size }
            
            StudyStatsDTO(
                totalTimeByTopic = totalTimeByTopic,
                totalTimeByMode = totalTimeByMode,
                itemsByStage = itemsByStage
            )
        }
    }
}

