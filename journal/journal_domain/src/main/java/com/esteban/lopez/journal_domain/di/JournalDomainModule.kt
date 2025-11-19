package com.esteban.lopez.journal_domain.di

import com.esteban.lopez.journal_domain.repository.JournalRepository
import com.esteban.lopez.journal_domain.use_cases.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object JournalDomainModule {
    @ViewModelScoped
    @Provides
    fun provideJournalUseCases(
        repository: JournalRepository
    ): JournalUseCases {
        return JournalUseCases(
            getQuestions = GetQuestions(repository),
            addQuestion = AddQuestion(repository),
            updateQuestion = UpdateQuestion(repository),
            deleteQuestion = DeleteQuestion(repository),
            createDailyJournal = CreateDailyJournal(repository),
            getJournalHistory = GetJournalHistory(repository),
            getJournalByDate = GetJournalByDate(repository),
            updateDailyJournal = UpdateDailyJournal(repository)
        )
    }
}

