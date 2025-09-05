package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.QuestionAnswer
import com.esteban.ruano.database.entities.QuestionAnswers
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.questions.CreateQuestionAnswerDTO
import com.esteban.ruano.models.questions.QuestionAnswerDTO
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

class QuestionAnswerService : BaseService() {

    fun create(userId: Int, dailyJournalId: UUID, answer: CreateQuestionAnswerDTO): UUID? {
        return transaction {
            val id = QuestionAnswers.insertOperation(userId) {
                insert {
                    it[this.answer] = answer.answer
                    it[mood] = answer.mood
                    it[question] = UUID.fromString(answer.questionId)
                    it[dailyJournal] = dailyJournalId
                    it[user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
            id
        }
    }

    fun getByDailyJournalId(dailyJournalId: UUID): List<QuestionAnswerDTO> {
        return transaction {
            QuestionAnswer.find {
                (QuestionAnswers.dailyJournal eq dailyJournalId) and (QuestionAnswers.status eq Status.ACTIVE)
            }.toList().map { it.toDTO() }
        }
    }

    fun getByQuestionId(questionId: UUID): List<QuestionAnswerDTO> {
        return transaction {
            QuestionAnswer.find {
                (QuestionAnswers.question eq questionId) and (QuestionAnswers.status eq Status.ACTIVE)
            }.toList().map { it.toDTO() }
        }
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = QuestionAnswers.deleteOperation(userId) {
                val updatedRows = QuestionAnswers.update({ (QuestionAnswers.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }
} 