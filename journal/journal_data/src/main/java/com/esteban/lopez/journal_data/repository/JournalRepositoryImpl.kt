package com.esteban.ruano.journal_data.repository

import com.esteban.lopez.journal_domain.repository.JournalRepository
import services.dailyjournal.DailyJournalService
import services.dailyjournal.models.QuestionAnswerDTO
import services.dailyjournal.models.QuestionDTO
import services.dailyjournal.models.QuestionType
import services.dailyjournal.models.DailyJournalResponse

class JournalRepositoryImpl(
    private val dailyJournalService: DailyJournalService
) : JournalRepository {

    override suspend fun getQuestions(): Result<List<QuestionDTO>> {
        return try {
            val questions = dailyJournalService.getQuestions()
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addQuestion(question: String, type: QuestionType): Result<Unit> {
        return try {
            dailyJournalService.addQuestion(question, type)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateQuestion(id: String, question: String, type: QuestionType): Result<QuestionDTO> {
        return try {
            val updatedQuestion = dailyJournalService.updateQuestion(id, question, type)
            Result.success(updatedQuestion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteQuestion(id: String): Result<Unit> {
        return try {
            dailyJournalService.deleteQuestion(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDailyJournal(
        date: String,
        summary: String,
        questionAnswers: List<QuestionAnswerDTO>
    ): Result<Unit> {
        return try {
            dailyJournalService.createDailyJournal(date, summary, questionAnswers)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getByDateRange(
        startDate: String,
        endDate: String,
        limit: Int,
        offset: Long
    ): Result<List<DailyJournalResponse>> {
        return try {
            val journals = dailyJournalService.getByDateRange(startDate, endDate, limit, offset)
            Result.success(journals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

