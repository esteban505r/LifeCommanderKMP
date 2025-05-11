package com.esteban.ruano.models.questions

import kotlinx.serialization.Serializable

@Serializable
data class QuestionAnswerDTO(
    val id: String,
    val questionId: String,
    val answer: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
) 