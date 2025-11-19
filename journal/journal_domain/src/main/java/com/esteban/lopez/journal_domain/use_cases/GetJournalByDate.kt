package com.esteban.lopez.journal_domain.use_cases

import com.esteban.lopez.journal_domain.repository.JournalRepository
import services.dailyjournal.models.DailyJournalResponse

class GetJournalByDate(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(date: String): Result<DailyJournalResponse?> {
        return repository.getJournalByDate(date)
    }
}

