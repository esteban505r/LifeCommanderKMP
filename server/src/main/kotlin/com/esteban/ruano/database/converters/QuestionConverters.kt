package com.esteban.ruano.database.converters

import formatDateTime
import com.esteban.ruano.database.entities.Question
import com.esteban.ruano.models.questions.CreateQuestionDTO
import com.esteban.ruano.models.questions.QuestionDTO
import com.esteban.ruano.models.questions.UpdateQuestionDTO

fun Question.toDTO(): QuestionDTO {
    return QuestionDTO(
        id = this.id.toString(),
        question = this.question
    )
}

fun QuestionDTO.toCreateQuestionDTO(): CreateQuestionDTO {
    return CreateQuestionDTO(
        question = this.question,
        createdAt = this.createdAt
    )
}

fun QuestionDTO.toUpdateQuestionDTO(): UpdateQuestionDTO {
    return UpdateQuestionDTO(
        question = this.question,
        updatedAt = this.updatedAt
    )
} 