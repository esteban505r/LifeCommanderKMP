package com.esteban.ruano.models.questions

import kotlinx.serialization.Serializable

@Serializable
data class QuestionDTO(
    val id: String,
    val question: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
) 