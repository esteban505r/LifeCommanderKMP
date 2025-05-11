package com.esteban.ruano.repository

import com.esteban.ruano.models.questions.CreateQuestionAnswerDTO
import com.esteban.ruano.models.questions.QuestionAnswerDTO
import com.esteban.ruano.service.QuestionAnswerService
import java.util.UUID

class QuestionAnswerRepository(private val questionAnswerService: QuestionAnswerService) {

    fun create(userId: Int, dailyJournalId: UUID, answer: CreateQuestionAnswerDTO): UUID? {
        return questionAnswerService.create(userId, dailyJournalId, answer)
    }

    fun getByDailyJournalId(dailyJournalId: UUID): List<QuestionAnswerDTO> {
        return questionAnswerService.getByDailyJournalId(dailyJournalId)
    }

    fun getByQuestionId(questionId: UUID): List<QuestionAnswerDTO> {
        return questionAnswerService.getByQuestionId(questionId)
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return questionAnswerService.delete(userId, id)
    }
} 