package com.esteban.ruano.models.questions

import kotlinx.serialization.Serializable

@Serializable
data class CreateQuestionDTO(
    val question: String,
    val type: QuestionType = QuestionType.TEXT,
    val createdAt: String? = null
) 