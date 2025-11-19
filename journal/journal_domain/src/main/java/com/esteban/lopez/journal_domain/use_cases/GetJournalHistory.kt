package com.esteban.lopez.journal_domain.use_cases

import com.esteban.lopez.journal_domain.repository.JournalRepository
import services.dailyjournal.models.DailyJournalResponse

class GetJournalHistory(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(
        startDate: String,
        endDate: String,
        limit: Int = 10,
        offset: Long = 0
    ): Result<List<DailyJournalResponse>> {
        return repository.getByDateRange(startDate, endDate, limit, offset)
    }
}

