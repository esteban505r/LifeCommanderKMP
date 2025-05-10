/*
package com.esteban.ruano.tasks_data.di

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core.interfaces.OperationListener
import com.esteban.ruano.core_data.workManager.tasks.factories.CheckTasksWorkerFactory
import com.esteban.ruano.tasks_data.datasources.TaskDataSource
import com.esteban.ruano.tasks_data.remote.TasksApi
import com.esteban.ruano.tasks_data.repository.FakeTaskRepository
import com.esteban.ruano.tasks_data.repository.TasksRepositoryImpl
import com.esteban.ruano.tasks_data.workers.factories.CheckTasksWorkerFactoryImpl
import com.esteban.ruano.tasks_domain.repository.TasksRepository
import com.esteban.ruano.test_core.utils.TestConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [TasksDataModule::class] // This is the original prod Dagger module we're replacing
)
object TestTasksDataModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }
    @Provides
    @Singleton
    fun provideTaskApi(client: OkHttpClient): TasksApi {
        return Retrofit.Builder()
            .baseUrl(TestConstants.BASE_URL)
            .addConverterFactory(
                Json.asConverterFactory("application/json".toMediaType())
            )
            .client(client)
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideTrackerRepository(
    ): TasksRepository {
        return FakeTaskRepository()
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
}*/
