package com.esteban.lopez.journal_domain.use_cases

import com.esteban.lopez.journal_domain.repository.JournalRepository
import services.dailyjournal.models.QuestionType

class AddQuestion(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(question: String, type: QuestionType): Result<Unit> {
        return repository.addQuestion(question, type)
    }
}

