package com.esteban.lopez.journal_domain.use_cases

import com.esteban.lopez.journal_domain.repository.JournalRepository

class DeleteQuestion(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.deleteQuestion(id)
    }
}

