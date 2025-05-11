package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID

object QuestionAnswers : UUIDTable() {
    val answer = text("answer")
    val question = reference("question_id", Questions, ReferenceOption.CASCADE)
    val dailyJournal = reference("daily_journal_id", DailyJournals, ReferenceOption.CASCADE)
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class QuestionAnswer(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<QuestionAnswer>(QuestionAnswers)
    var answer by QuestionAnswers.answer
    var question by Question referencedOn QuestionAnswers.question
    var dailyJournal by DailyJournal referencedOn QuestionAnswers.dailyJournal
    var user by User referencedOn QuestionAnswers.user
    var status by QuestionAnswers.status
} 