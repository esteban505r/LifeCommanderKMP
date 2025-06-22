package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Question
import com.esteban.ruano.models.questions.CreateQuestionDTO
import com.esteban.ruano.models.questions.QuestionDTO
import com.esteban.ruano.models.questions.QuestionType
import com.esteban.ruano.models.questions.UpdateQuestionDTO

fun Question.toDTO(): QuestionDTO {
    return QuestionDTO(
        id = this.id.toString(),
        question = this.question,
        type = this.type.name
    )
}

fun QuestionDTO.toCreateQuestionDTO(): CreateQuestionDTO {
    return CreateQuestionDTO(
        question = this.question,
        type = QuestionType.fromString(this.type),
        createdAt = this.createdAt
    )
}

fun QuestionDTO.toUpdateQuestionDTO(): UpdateQuestionDTO {
    return UpdateQuestionDTO(
        question = this.question,
        type = QuestionType.fromString(this.type),
        updatedAt = this.updatedAt
    )
} 