package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.DailyJournal
import com.esteban.ruano.database.entities.DailyJournals
import com.esteban.ruano.database.entities.Question
import com.esteban.ruano.database.entities.Questions
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.dailyjournal.*
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import kotlinx.datetime.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

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
        val journals =  transaction {
            DailyJournal.find {
                (DailyJournals.user eq userId) and
                (DailyJournals.status eq Status.ACTIVE) and
                (DailyJournals.date greaterEq startDate) and
                (DailyJournals.date lessEq endDate)
            }.limit(limit, offset).toList().map { it.toDTO() }
        }

        val result = journals.map {
            val questionAnswers = questionAnswerService.getByDailyJournalId(UUID.fromString(it.id))
            it.copy(questionAnswers = questionAnswers)
        }

        return result
    }

    fun getByIdAndUserId(id: UUID, userId: Int): DailyJournalDTO? {
        return transaction {
            DailyJournal.find {
                (DailyJournals.id eq id) and (DailyJournals.user eq userId) and (DailyJournals.status eq Status.ACTIVE)
            }.firstOrNull()?.toDTO()
        }
    }

    fun getJournalEntryByDate(userId: Int, date: LocalDate): JournalHistoryEntry? {
        val journal = transaction {
            DailyJournal.find {
                (DailyJournals.user eq userId) and 
                (DailyJournals.status eq Status.ACTIVE) and
                (DailyJournals.date eq date)
            }.firstOrNull()
        }

        return journal?.let { j ->
            val questionAnswers = questionAnswerService.getByDailyJournalId(j.id.value)
            val questions = questionAnswers.map { answer ->

                val question = Question.find { Questions.id eq UUID.fromString(answer.questionId) }.firstOrNull()


                if (question == null) {
                    throw IllegalStateException("Question with ID ${answer.questionId} not found")
                }

                QuestionWithAnswer(
                    id = answer.questionId,
                    question = question.question,
                    type = question.type,
                    answer = answer.answer
                )
            }
            JournalHistoryEntry(
                date = j.date.formatDefault(),
                questions = questions
            )
        }
    }

    fun getJournalEntriesInRange(userId: Int, startDate: LocalDate, endDate: LocalDate): List<JournalHistoryEntry> {
        val journals = transaction {
            DailyJournal.find {
                (DailyJournals.user eq userId) and
                (DailyJournals.status eq Status.ACTIVE) and
                (DailyJournals.date greaterEq startDate) and
                (DailyJournals.date lessEq endDate)
            }.orderBy(DailyJournals.date to SortOrder.DESC).toList()
        }

        return journals.map { journal ->
            val questionAnswers = questionAnswerService.getByDailyJournalId(journal.id.value)
            val questions = questionAnswers.map { answer ->

                val question = Question.find { Questions.id eq UUID.fromString(answer.questionId) }.firstOrNull()

                if (question == null) {
                    throw IllegalStateException("Question with ID ${answer.questionId} not found")
                }

                QuestionWithAnswer(
                    id = answer.questionId,
                    question = question.question,
                    type = question.type,
                    answer = answer.answer
                )
            }
            JournalHistoryEntry(
                date = journal.date.formatDefault(),
                questions = questions
            )
        }
    }
} 