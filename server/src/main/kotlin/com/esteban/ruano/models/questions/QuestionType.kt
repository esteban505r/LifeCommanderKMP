package com.esteban.ruano.models.questions

import kotlinx.serialization.Serializable

@Serializable
enum class QuestionType {
    TEXT,
    MOOD;

    companion object {
        fun fromString(type: String?): QuestionType {
            return entries.find { it.name.uppercase().equals(type?.uppercase(), ignoreCase = true) }
            ?: TEXT
        }
    }
} 