package com.esteban.ruano.database.converters

import formatDate
import formatDateTime
import com.esteban.ruano.database.entities.DailyJournal
import com.esteban.ruano.models.dailyjournal.CreateDailyJournalDTO
import com.esteban.ruano.models.dailyjournal.DailyJournalDTO
import com.esteban.ruano.models.dailyjournal.UpdateDailyJournalDTO

fun DailyJournal.toDTO(): DailyJournalDTO {
    return DailyJournalDTO(
        id = this.id.toString(),
        date = formatDate(this.date),
        summary = this.summary,
        pomodoros = emptyList(),
        questionAnswers = emptyList(),
    )
}

