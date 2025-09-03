package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.questions.MoodType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.*

object QuestionAnswers : UUIDTable() {
    val answer = text("answer")
    val mood = enumerationByName("mood", 20, MoodType::class).nullable()
    val question = reference("question_id", Questions, ReferenceOption.CASCADE)
    val dailyJournal = reference("daily_journal_id", DailyJournals, ReferenceOption.CASCADE)
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class QuestionAnswer(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<QuestionAnswer>(QuestionAnswers)
    var answer by QuestionAnswers.answer
    var mood by QuestionAnswers.mood
    var question by Question referencedOn QuestionAnswers.question
    var dailyJournal by DailyJournal referencedOn QuestionAnswers.dailyJournal
    var user by User referencedOn QuestionAnswers.user
    var status by QuestionAnswers.status
} 