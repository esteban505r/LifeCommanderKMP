package com.esteban.ruano.timers_data.di

import com.esteban.ruano.core.di.WebSocketHttpClient
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.lifecommander.services.timers.TimerService
import com.esteban.ruano.timers_data.repository.TimerWebSocketRepositoryImpl
import com.esteban.ruano.timers_data.repository.TimersRepositoryImpl
import com.esteban.ruano.timers_domain.repository.TimerWebSocketRepository
import com.esteban.ruano.timers_domain.repository.TimersRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimersDataModule {

    @Provides
    @Singleton
    fun provideTimerService(httpClient: HttpClient): TimerService {
        return TimerService(httpClient)
    }

    @Provides
    @Singleton
    fun provideTimersRepository(
        timerService: TimerService,
        preferences: Preferences
    ): TimersRepository {
        return TimersRepositoryImpl(
            timerService = timerService,
            preferences = preferences
        )
    }

    @Provides
    @Singleton
    fun provideTimerWebSocketRepository(
        @WebSocketHttpClient httpClient: HttpClient,
        preferences: Preferences
    ): TimerWebSocketRepository {
        return TimerWebSocketRepositoryImpl(
            httpClient = httpClient,
            preferences = preferences
        )
    }
}

