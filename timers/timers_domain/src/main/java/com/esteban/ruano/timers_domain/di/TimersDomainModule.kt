package com.esteban.ruano.timers_domain.di

import com.esteban.ruano.timers_domain.repository.TimersRepository
import com.esteban.ruano.timers_domain.use_case.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object TimersDomainModule {

    @ViewModelScoped
    @Provides
    fun provideTimerUseCases(
        repository: TimersRepository
    ): TimerUseCases {
        return TimerUseCases(
            getTimerLists = GetTimerLists(repository),
            getTimerList = GetTimerList(repository),
            createTimerList = CreateTimerList(repository),
            updateTimerList = UpdateTimerList(repository),
            deleteTimerList = DeleteTimerList(repository),
            createTimer = CreateTimer(repository),
            updateTimer = UpdateTimer(repository),
            deleteTimer = DeleteTimer(repository),
            startTimer = StartTimer(repository),
            pauseTimer = PauseTimer(repository),
            resumeTimer = ResumeTimer(repository),
            stopTimer = StopTimer(repository)
        )
    }
}

