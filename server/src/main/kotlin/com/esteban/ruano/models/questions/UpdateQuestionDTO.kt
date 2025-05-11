package com.esteban.ruano.models.questions

import kotlinx.serialization.Serializable

@Serializable
data class UpdateQuestionDTO(
    val question: String? = null,
    val updatedAt: String? = null
) 