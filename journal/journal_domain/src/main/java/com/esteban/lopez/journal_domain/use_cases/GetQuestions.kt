package com.esteban.lopez.journal_domain.use_cases

import com.esteban.lopez.journal_domain.repository.JournalRepository
import services.dailyjournal.models.QuestionDTO

class GetQuestions(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(): Result<List<QuestionDTO>> {
        return repository.getQuestions()
    }
}

