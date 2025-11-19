package com.esteban.ruano.journal_presentation.intent

import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import services.dailyjournal.models.QuestionType
import kotlinx.datetime.LocalDate

sealed class JournalIntent : UserIntent {
    data object InitializeJournal : JournalIntent()
    data object LoadQuestions : JournalIntent()
    data class AddAnswer(val questionId: String, val answer: String) : JournalIntent()
    data class AddQuestion(val question: String, val type: QuestionType) : JournalIntent()
    data class UpdateQuestion(val id: String, val question: String, val type: QuestionType) : JournalIntent()
    data class DeleteQuestion(val id: String) : JournalIntent()
    data object CompleteDailyJournal : JournalIntent()
    data object ResetJournal : JournalIntent()
    data class GetHistoryByDateRange(val startDate: LocalDate, val endDate: LocalDate) : JournalIntent()
    data object ResetError : JournalIntent()
}

sealed class JournalEffect : Effect {
    data class ShowError(val message: String) : JournalEffect()
    data object NavigateToHistory : JournalEffect()
}

