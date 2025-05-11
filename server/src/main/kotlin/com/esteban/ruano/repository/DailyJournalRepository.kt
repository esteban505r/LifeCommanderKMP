package com.esteban.ruano.repository

import kotlinx.datetime.*
import com.esteban.ruano.models.dailyjournal.CreateDailyJournalDTO
import com.esteban.ruano.models.dailyjournal.DailyJournalDTO
import com.esteban.ruano.models.dailyjournal.UpdateDailyJournalDTO
import com.esteban.ruano.service.DailyJournalService
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
            parseDate(startDate),
            parseDate(endDate),
            limit,
            offset
        )
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