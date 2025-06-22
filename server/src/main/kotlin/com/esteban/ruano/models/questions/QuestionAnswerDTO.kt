package com.esteban.ruano.models.questions

import kotlinx.serialization.Serializable

@Serializable
data class QuestionAnswerDTO(
    val id: String,
    val questionId: String,
    val question: String,
    val answer: String,
    val type: String,
    val mood: MoodType? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) 