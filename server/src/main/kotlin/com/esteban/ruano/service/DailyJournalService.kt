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
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

class DailyJournalService(
    private val pomodoroService: PomodoroService,
    private val questionAnswerService: QuestionAnswerService
) : BaseService() {

    fun create(userId: Int, journal: CreateDailyJournalDTO): UUID? {
        return transaction {
            val journalDate = journal.date.toLocalDate()
            
            // Try to find existing journal entry first
            var existingJournal = DailyJournal.find {
                (DailyJournals.user eq userId) and
                (DailyJournals.date eq journalDate) and
                (DailyJournals.status eq Status.ACTIVE)
            }.firstOrNull()
            
            val id = if (existingJournal != null) {
                // Update existing journal entry
                DailyJournals.updateOperation(userId) {
                    val updatedRows = update({ (DailyJournals.id eq existingJournal.id.value) }) {
                        it[summary] = journal.summary
                    }
                    if (updatedRows > 0) existingJournal.id.value else null
                }
            } else {
                // Try to insert new journal entry
                // If unique constraint violation occurs, retry with update
                try {
                    DailyJournals.insertOperation(userId) {
                        insert {
                            it[date] = journalDate
                            it[summary] = journal.summary
                            it[user] = userId
                        }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
                    } ?: run {
                        // If insert returned null, try to find existing (race condition occurred)
                        existingJournal = DailyJournal.find {
                            (DailyJournals.user eq userId) and
                            (DailyJournals.date eq journalDate) and
                            (DailyJournals.status eq Status.ACTIVE)
                        }.firstOrNull()
                        
                        existingJournal?.let {
                            DailyJournals.updateOperation(userId) {
                                val updatedRows = update({ (DailyJournals.id eq it.id.value) }) {
                                    it[summary] = journal.summary
                                }
                                if (updatedRows > 0) it.id.value else null
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Handle unique constraint violation or other database errors
                    // Try to find and update existing journal
                    existingJournal = DailyJournal.find {
                        (DailyJournals.user eq userId) and
                        (DailyJournals.date eq journalDate) and
                        (DailyJournals.status eq Status.ACTIVE)
                    }.firstOrNull()
                    
                    existingJournal?.let {
                        DailyJournals.updateOperation(userId) {
                            val updatedRows = update({ (DailyJournals.id eq it.id.value) }) {
                                it[summary] = journal.summary
                            }
                            if (updatedRows > 0) it.id.value else null
                        }
                    }
                }
            }
            
            id?.let { journalId ->
                // Always delete existing question answers and recreate them
                // This ensures consistency whether it's a new or updated journal
                val existingAnswers = questionAnswerService.getByDailyJournalId(journalId)
                existingAnswers.forEach { answer ->
                    questionAnswerService.delete(userId, UUID.fromString(answer.id))
                }
                
                // Create new question answers
                journal.questionAnswers.forEach { answer ->
                    questionAnswerService.create(userId, journalId, answer)
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
            }.limit(limit).offset(offset*limit).toList().map { it.toDTO() }
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
            }.limit(limit).offset(offset*limit).toList().map { it.toDTO() }
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