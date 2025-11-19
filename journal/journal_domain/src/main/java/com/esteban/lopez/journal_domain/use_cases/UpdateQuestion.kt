package com.esteban.lopez.journal_domain.use_cases

import com.esteban.lopez.journal_domain.repository.JournalRepository
import services.dailyjournal.models.QuestionDTO
import services.dailyjournal.models.QuestionType

class UpdateQuestion(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(id: String, question: String, type: QuestionType): Result<QuestionDTO> {
        return repository.updateQuestion(id, question, type)
    }
}

