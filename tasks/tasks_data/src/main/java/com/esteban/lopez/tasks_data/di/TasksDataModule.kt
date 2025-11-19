package com.esteban.ruano.tasks_data.di

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.lopez.core.utils.AppConstants
import com.esteban.ruano.core_data.local.HistoryTrackDao
import com.esteban.ruano.core_data.models.Local
import com.esteban.ruano.core_data.models.Remote
import com.esteban.ruano.core_data.workManager.tasks.factories.CheckTasksWorkerFactory
import com.esteban.ruano.tasks_data.datasources.TaskDataSource
import com.esteban.ruano.tasks_data.datasources.TaskLocalDataSource
import com.esteban.ruano.tasks_data.datasources.TaskRemoteDataSource
import com.esteban.ruano.tasks_data.local.TaskDao
import com.esteban.ruano.tasks_data.remote.TasksApi
import com.esteban.ruano.tasks_data.repository.TasksRepositoryImpl
import com.esteban.ruano.tasks_data.workers.factories.CheckTasksWorkerFactoryImpl
import com.esteban.ruano.tasks_domain.repository.TasksRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import services.tags.TagService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TasksDataModule {
    @Provides
    @Singleton
    fun provideTaskApi(client: OkHttpClient): TasksApi {
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
    fun provideTaskDataSource(api: TasksApi): TaskDataSource {
        return TaskRemoteDataSource(
            api = api
        )
    }

    @Local
    @Provides
    @Singleton
    fun provideTaskLocalDataSource(
        taskDao: TaskDao,
        historyTrackDao: HistoryTrackDao
    ): TaskDataSource {
        return TaskLocalDataSource(
            taskDao = taskDao,
            historyTrackDao = historyTrackDao
        )
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        @Remote remoteDataSource: TaskDataSource,
        @Local localDataSource: TaskDataSource,
        networkHelper: NetworkHelper,
        preferences: Preferences,
    ): TasksRepository {
        return TasksRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            networkHelper = networkHelper,
            preferences = preferences,
        )
    }

    @Provides
    @Singleton
    fun provideCheckTasksWorkerFactory(
        repository: TasksRepository,
    ): CheckTasksWorkerFactory {
        return CheckTasksWorkerFactoryImpl(
            repository = repository
        )
    }

    @Provides
    @Singleton
    fun provideTagService(client: HttpClient): TagService {
        return TagService(client)
    }
}