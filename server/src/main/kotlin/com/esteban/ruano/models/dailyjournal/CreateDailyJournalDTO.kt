package com.esteban.ruano.models.dailyjournal

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.questions.CreateQuestionAnswerDTO

@Serializable
data class CreateDailyJournalDTO(
    val date: String,
    val summary: String,
    val questionAnswers: List<CreateQuestionAnswerDTO>,
    val createdAt: String? = null
) 