package com.esteban.lopez.journal_domain.repository

import services.dailyjournal.models.QuestionAnswerDTO
import services.dailyjournal.models.QuestionDTO
import services.dailyjournal.models.QuestionType
import services.dailyjournal.models.DailyJournalResponse

interface JournalRepository {
    suspend fun getQuestions(): Result<List<QuestionDTO>>
    suspend fun addQuestion(question: String, type: QuestionType): Result<Unit>
    suspend fun updateQuestion(id: String, question: String, type: QuestionType): Result<QuestionDTO>
    suspend fun deleteQuestion(id: String): Result<Unit>
    suspend fun createDailyJournal(
        date: String,
        summary: String,
        questionAnswers: List<QuestionAnswerDTO>
    ): Result<Unit>
    suspend fun getByDateRange(
        startDate: String,
        endDate: String,
        limit: Int = 10,
        offset: Long = 0
    ): Result<List<DailyJournalResponse>>
    suspend fun getJournalByDate(date: String): Result<DailyJournalResponse?>
    suspend fun updateDailyJournal(
        id: String,
        summary: String,
        questionAnswers: List<QuestionAnswerDTO>
    ): Result<Unit>
}

