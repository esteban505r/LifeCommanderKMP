package com.esteban.ruano.workout_data.di

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core.utils.AppConstants
import com.esteban.ruano.core_data.models.Local
import com.esteban.ruano.core_data.models.Remote
import com.esteban.ruano.workout_data.datasources.WorkoutDataSource
import com.esteban.ruano.workout_data.datasources.WorkoutLocalDataSource
import com.esteban.ruano.workout_data.datasources.WorkoutRemoteDataSource
import com.esteban.ruano.workout_data.local.WorkoutDao
import com.esteban.ruano.workout_data.remote.WorkoutApi
import com.esteban.ruano.workout_data.repository.WorkoutRepositoryImpl
import com.esteban.ruano.workout_domain.repository.WorkoutRepository
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
object WorkoutDataModule {
    @Provides
    @Singleton
    fun provideTaskApi(client: OkHttpClient): WorkoutApi {
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create()
    }

    @Remote
    @Provides
    @Singleton
    fun provideWorkoutRemoteDataSource(api: WorkoutApi): WorkoutDataSource {
        return WorkoutRemoteDataSource(
            api = api
        )
    }

    @Local
    @Provides
    @Singleton
    fun provideWorkoutLocalDataSource(
        workoutDao: WorkoutDao
    ): WorkoutDataSource {
        return WorkoutLocalDataSource(
            workoutDao = workoutDao
        )
    }

    @Provides
    @Singleton
    fun provideTrackerRepository(
        @Local localDataSource: WorkoutDataSource,
        @Remote remoteDataSource: WorkoutDataSource,
        networkHelper: NetworkHelper,
        preferences: Preferences
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            networkHelper = networkHelper,
            preferences = preferences
        )
    }
}