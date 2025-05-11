package com.esteban.ruano.service

import kotlinx.datetime.*
import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.DailyJournal
import com.esteban.ruano.database.entities.DailyJournals
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.dailyjournal.CreateDailyJournalDTO
import com.esteban.ruano.models.dailyjournal.DailyJournalDTO
import com.esteban.ruano.models.dailyjournal.UpdateDailyJournalDTO
import com.esteban.ruano.models.questions.CreateQuestionAnswerDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class DailyJournalService(
    private val pomodoroService: PomodoroService,
    private val questionAnswerService: QuestionAnswerService
) : BaseService() {

    fun create(userId: Int, journal: CreateDailyJournalDTO): UUID? {
        return transaction {
            val id = DailyJournals.insertOperation(userId) {
                insert {
                    it[date] = journal.date.toLocalDate()
                    it[summary] = journal.summary
                    it[user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
            id?.let {
                // Create question answers
                journal.questionAnswers.forEach { answer ->
                    questionAnswerService.create(userId, it, answer)
                }
            }
            id
        }
    }

    fun update(userId: Int, id: UUID, journal: UpdateDailyJournalDTO): Boolean {
        return transaction {
            val updatedRow = DailyJournals.updateOperation(userId) {
                val updatedRows = update({ (DailyJournals.id eq id) }) { row ->
                    journal.summary?.let { row[summary] = it }
                }
                if (updatedRows > 0) id else null
            }
            updatedRow != null
        }
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = DailyJournals.deleteOperation(userId) {
                val updatedRows = DailyJournals.update({ (DailyJournals.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }

    fun getByUserId(userId: Int, limit: Int, offset: Long): List<DailyJournalDTO> {
        return transaction {
            DailyJournal.find {
                (DailyJournals.user eq userId) and (DailyJournals.status eq Status.ACTIVE)
            }.limit(limit, offset).toList().map { it.toDTO() }
        }
    }

    fun getByDateRange(
        userId: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
        offset: Long
    ): List<DailyJournalDTO> {
        return transaction {
            DailyJournal.find {
                (DailyJournals.user eq userId) and
                (DailyJournals.status eq Status.ACTIVE) and
                (DailyJournals.date greaterEq startDate) and
                (DailyJournals.date lessEq endDate)
            }.limit(limit, offset).toList().map { it.toDTO() }
        }
    }

    fun getByIdAndUserId(id: UUID, userId: Int): DailyJournalDTO? {
        return transaction {
            DailyJournal.find {
                (DailyJournals.id eq id) and (DailyJournals.user eq userId) and (DailyJournals.status eq Status.ACTIVE)
            }.firstOrNull()?.toDTO()
        }
    }
} 