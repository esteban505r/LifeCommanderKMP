package com.esteban.lopez.journal_domain.use_cases

data class JournalUseCases(
    val getQuestions: GetQuestions,
    val addQuestion: AddQuestion,
    val updateQuestion: UpdateQuestion,
    val deleteQuestion: DeleteQuestion,
    val createDailyJournal: CreateDailyJournal,
    val getJournalHistory: GetJournalHistory,
    val getJournalByDate: GetJournalByDate,
    val updateDailyJournal: UpdateDailyJournal
)

