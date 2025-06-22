package com.esteban.ruano.models.dailyjournal

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.questions.QuestionType
import java.time.LocalDate

@Serializable
data class JournalHistoryEntry(
    val date: String,
    val questions: List<QuestionWithAnswer>
)

@Serializable
data class QuestionWithAnswer(
    val id: String,
    val question: String,
    val type: QuestionType,
    val answer: String
) 