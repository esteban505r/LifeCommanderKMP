package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.Question
import com.esteban.ruano.database.entities.Questions
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.questions.CreateQuestionDTO
import com.esteban.ruano.models.questions.QuestionDTO
import com.esteban.ruano.models.questions.UpdateQuestionDTO
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

class QuestionService : BaseService() {

    fun create(userId: Int, question: CreateQuestionDTO): UUID? {
        return transaction {
            val id = Questions.insertOperation(userId) {
                insert {
                    it[this.question] = question.question
                    it[type] = question.type
                    it[user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
            id
        }
    }

    fun update(userId: Int, id: UUID, question: UpdateQuestionDTO): Boolean {
        return transaction {
            val updatedRow = Questions.updateOperation(userId) {
                val updatedRows = update({ (Questions.id eq id) }) { row ->
                    question.question?.let { row[Questions.question] = it }
                    question.type?.let { row[Questions.type] = it }
                }
                if (updatedRows > 0) id else null
            }
            updatedRow != null
        }
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = Questions.deleteOperation(userId) {
                val updatedRows = Questions.update({ (Questions.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }

    fun getByUserId(userId: Int, limit: Int, offset: Long): List<QuestionDTO> {
        return transaction {
            Question.find {
                (Questions.user eq userId) and (Questions.status eq Status.ACTIVE)
            }.limit(limit).offset(offset*limit).toList().map { it.toDTO() }
        }
    }

    fun getByIdAndUserId(id: UUID, userId: Int): QuestionDTO? {
        return transaction {
            Question.find {
                (Questions.id eq id) and (Questions.user eq userId) and (Questions.status eq Status.ACTIVE)
            }.firstOrNull()?.toDTO()
        }
    }
} 