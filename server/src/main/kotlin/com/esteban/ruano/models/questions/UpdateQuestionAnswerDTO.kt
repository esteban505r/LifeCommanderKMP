package com.esteban.ruano.models.questions

import kotlinx.serialization.Serializable

@Serializable
data class UpdateQuestionAnswerDTO(
    val answer: String? = null,
    val updatedAt: String? = null
) 