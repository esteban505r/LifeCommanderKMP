package com.esteban.ruano.habits_data.di

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core.utils.Constants
import com.esteban.ruano.core_data.local.HistoryTrackDao
import com.esteban.ruano.core_data.models.Local
import com.esteban.ruano.core_data.models.Remote
import com.esteban.ruano.core_data.workManager.habits.factories.CheckHabitsWorkerFactory
import com.esteban.ruano.habits_data.datasources.HabitsDataSource
import com.esteban.ruano.habits_data.datasources.HabitsLocalDataSource
import com.esteban.ruano.habits_data.datasources.HabitsRemoteDataSource
import com.esteban.ruano.habits_data.local.HabitsDao
import com.esteban.ruano.habits_data.remote.HabitsApi
import com.esteban.ruano.habits_data.repository.HabitsRepositoryImpl
import com.esteban.ruano.habits_data.workers.factories.CheckHabitsWorkerFactoryImpl
import com.esteban.ruano.habits_domain.repository.HabitsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HabitsDataModule {

    @Provides
    @Singleton
    fun provideTaskApi(client: OkHttpClient): HabitsApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create()
    }

    @Remote
    @Provides
    @Singleton
    fun provideHabitRemoteDataSource(api: HabitsApi): HabitsDataSource {
        return HabitsRemoteDataSource(
            habitsApi = api
        )
    }

    @Local
    @Provides
    @Singleton
    fun provideTaskLocalDataSource(
        habitDao: HabitsDao,
        historyTrackDao: HistoryTrackDao
    ): HabitsDataSource {
        return HabitsLocalDataSource(
            habitsDao = habitDao,
            historyTrackDao = historyTrackDao
        )
    }


    @Provides
    @Singleton
    fun provideTrackerRepository(
        @Remote remoteDataSource: HabitsDataSource,
        @Local localDataSource: HabitsDataSource,
        networkHelper: NetworkHelper,
        preferences: Preferences

    ): HabitsRepository {
        return HabitsRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            networkHelper = networkHelper,
            preferences = preferences
        )
    }

    @Provides
    @Singleton
    fun provideCheckHabitsWorkerFactory(
        repository: HabitsRepository,
    ): CheckHabitsWorkerFactory {
        return CheckHabitsWorkerFactoryImpl(
            repository = repository
        )
    }
}