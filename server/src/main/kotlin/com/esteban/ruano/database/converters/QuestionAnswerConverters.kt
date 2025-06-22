package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.QuestionAnswer
import com.esteban.ruano.models.questions.QuestionAnswerDTO

fun QuestionAnswer.toDTO(): QuestionAnswerDTO {
    return QuestionAnswerDTO(
        id = this.id.toString(),
        questionId = this.question.id.toString(),
        question = this.question.question,
        type = this.question.type.name.uppercase(),
        answer = this.answer,
        mood = this.mood
    )
} 