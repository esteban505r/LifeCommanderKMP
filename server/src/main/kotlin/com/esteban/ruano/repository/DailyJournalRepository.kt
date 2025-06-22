package com.esteban.ruano.repository

import kotlinx.datetime.*
import com.esteban.ruano.models.dailyjournal.CreateDailyJournalDTO
import com.esteban.ruano.models.dailyjournal.DailyJournalDTO
import com.esteban.ruano.models.dailyjournal.UpdateDailyJournalDTO
import com.esteban.ruano.models.dailyjournal.JournalHistoryEntry
import com.esteban.ruano.service.DailyJournalService
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.utils.parseDate
import java.util.UUID

class DailyJournalRepository(private val dailyJournalService: DailyJournalService) {

    fun getAll(userId: Int, limit: Int, offset: Long): List<DailyJournalDTO> {
        return dailyJournalService.getByUserId(userId, limit, offset)
    }

    fun getAllByDateRange(
        userId: Int,
        startDate: String,
        endDate: String,
        limit: Int,
        offset: Long
    ): List<DailyJournalDTO> {
        return dailyJournalService.getByDateRange(
            userId,
            startDate.toLocalDate(),
            parseDate(endDate),
            limit,
            offset
        )
    }

    fun getJournalEntryByDate(userId: Int, date: LocalDate): JournalHistoryEntry? {
        return dailyJournalService.getJournalEntryByDate(userId, date)
    }

    fun getJournalEntriesInRange(userId: Int, startDate: LocalDate, endDate: LocalDate): List<JournalHistoryEntry> {
        return dailyJournalService.getJournalEntriesInRange(userId, startDate, endDate)
    }

    fun create(userId: Int, journal: CreateDailyJournalDTO): UUID? {
        return dailyJournalService.create(userId, journal)
    }

    fun update(userId: Int, id: UUID, journal: UpdateDailyJournalDTO): Boolean {
        return dailyJournalService.update(userId, id, journal)
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return dailyJournalService.delete(userId, id)
    }

    fun getByIdAndUserId(id: UUID, userId: Int): DailyJournalDTO? {
        return dailyJournalService.getByIdAndUserId(id, userId)
    }
} 