package com.esteban.ruano.models.dailyjournal

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.pomodoros.PomodoroDTO
import com.esteban.ruano.models.questions.QuestionAnswerDTO

@Serializable
data class DailyJournalDTO(
    val id: String,
    val date: String,
    val summary: String,
    val pomodoros: List<PomodoroDTO>,
    val questionAnswers: List<QuestionAnswerDTO>,
    val createdAt: String? = null,
    val updatedAt: String? = null
) 