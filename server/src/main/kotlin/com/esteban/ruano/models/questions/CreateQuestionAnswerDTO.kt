package com.esteban.ruano.models.questions

import kotlinx.serialization.Serializable

@Serializable
data class CreateQuestionAnswerDTO(
    val questionId: String,
    val answer: String,
    val createdAt: String? = null
) 