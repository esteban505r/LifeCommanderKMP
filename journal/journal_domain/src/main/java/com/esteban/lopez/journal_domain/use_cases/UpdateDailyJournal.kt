package com.esteban.lopez.journal_domain.use_cases

import com.esteban.lopez.journal_domain.repository.JournalRepository
import services.dailyjournal.models.QuestionAnswerDTO

class UpdateDailyJournal(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(
        id: String,
        summary: String,
        questionAnswers: List<QuestionAnswerDTO>
    ): Result<Unit> {
        return repository.updateDailyJournal(id, summary, questionAnswers)
    }
}

