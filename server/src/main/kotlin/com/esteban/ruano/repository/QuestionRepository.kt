package com.esteban.ruano.repository

import com.esteban.ruano.models.questions.CreateQuestionDTO
import com.esteban.ruano.models.questions.QuestionDTO
import com.esteban.ruano.models.questions.UpdateQuestionDTO
import com.esteban.ruano.service.QuestionService
import java.util.UUID

class QuestionRepository(private val questionService: QuestionService) {

    fun getAll(userId: Int, limit: Int, offset: Long): List<QuestionDTO> {
        return questionService.getByUserId(userId, limit, offset)
    }

    fun create(userId: Int, question: CreateQuestionDTO): UUID? {
        return questionService.create(userId, question)
    }

    fun update(userId: Int, id: UUID, question: UpdateQuestionDTO): Boolean {
        return questionService.update(userId, id, question)
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return questionService.delete(userId, id)
    }

    fun getByIdAndUserId(id: UUID, userId: Int): QuestionDTO? {
        return questionService.getByIdAndUserId(id, userId)
    }
} 