package com.esteban.ruano.models.dailyjournal

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.questions.CreateQuestionAnswerDTO

@Serializable
data class UpdateDailyJournalDTO(
    val summary: String? = null,
    val questionAnswers: List<CreateQuestionAnswerDTO>? = null,
    val updatedAt: String? = null
) 