package com.esteban.ruano.journal_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import services.dailyjournal.models.QuestionAnswerDTO
import services.dailyjournal.models.QuestionDTO
import services.dailyjournal.models.DailyJournalResponse

data class JournalState(
    val questions: List<QuestionDTO> = emptyList(),
    val questionAnswers: List<QuestionAnswerDTO> = emptyList(),
    val journalHistory: List<DailyJournalResponse> = emptyList(),
    val showQuestions: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isCompleted: Boolean = false,
): ViewState

