package com.esteban.ruano.journal_data.di

import android.content.Context
import com.esteban.lopez.core.utils.AppConstants
import com.esteban.lopez.journal_domain.repository.JournalRepository
import com.esteban.ruano.core.data.preferences.dataStore
import com.esteban.ruano.journal_data.repository.JournalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import services.auth.TokenStorageImpl
import services.dailyjournal.DailyJournalService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object JournalDataModule {

    @Provides
    @Singleton
    fun provideJournalRepository(
        dailyJournalService: DailyJournalService
    ): JournalRepository {
        return JournalRepositoryImpl(
            dailyJournalService
        )
    }

        @Provides
        @Singleton
        fun provideTokenStorageImpl(
            @ApplicationContext context: Context
        ): TokenStorageImpl {
            return TokenStorageImpl(context.dataStore)
        }

        @Provides
        @Singleton
        fun provideDailyJournalService(
            httpClient: HttpClient,
            tokenStorageImpl: TokenStorageImpl
        ): DailyJournalService {
            return DailyJournalService(
                baseUrl = AppConstants.BASE_URL,
                tokenStorageImpl = tokenStorageImpl,
                httpClient = httpClient
            )
        }

}

