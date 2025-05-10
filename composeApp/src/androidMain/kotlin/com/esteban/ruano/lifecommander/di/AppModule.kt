package com.esteban.ruano.lifecommander.di

import com.esteban.ruano.core_data.helpers.AlarmHelper
import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import androidx.work.WorkManager
import com.esteban.ruano.core.data.preferences.DataStorePreferences
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.domain.use_case.FilterOutDigits
import com.esteban.ruano.core.di.HabitsAlarmReceiverClass
import com.esteban.ruano.core.di.TasksAlarmReceiverClass
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core.helpers.NetworkHelperImpl
import com.esteban.ruano.core.interfaces.OperationListener
import com.esteban.ruano.core.utils.Constants
import com.esteban.ruano.core_data.local.HistoryTrackDao
import com.esteban.ruano.core_data.models.Local
import com.esteban.ruano.core_data.models.Remote
import com.esteban.ruano.habits_data.local.HabitsDao
import com.esteban.ruano.habits_domain.repository.HabitsRepository
import com.esteban.ruano.lifecommander.data.datasource.SyncDataSource
import com.esteban.ruano.lifecommander.data.datasource.SyncLocalDataSource
import com.esteban.ruano.lifecommander.data.datasource.SyncRemoteDataSource
import com.esteban.ruano.lifecommander.data.remote.SyncApi
import com.esteban.ruano.lifecommander.data.repository.SyncRepositoryImpl
import com.esteban.ruano.lifecommander.database.AppDatabase
import com.esteban.ruano.lifecommander.domain.repository.SyncRepository
import com.esteban.ruano.lifecommander.utilities.AlarmHelperImpl
import com.esteban.ruano.lifecommander.utilities.HabitsAlarmReceiver
import com.esteban.ruano.lifecommander.utilities.SyncUtils
import com.esteban.ruano.lifecommander.utilities.TasksAlarmReceiver
import com.esteban.ruano.core_ui.WorkManagerUtils
import com.esteban.ruano.lifecommander.utilities.workmanager.WorkManagerUtilsImpl
import com.esteban.ruano.tasks_data.local.TaskDao
import com.esteban.ruano.tasks_domain.repository.TasksRepository
import com.esteban.ruano.workout_data.local.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Singleton
    @Provides
    fun provideYourDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        AppDatabase::class.java,
        "life_commander_db"
    ).fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideTaskDao(appDatabase: AppDatabase) = appDatabase.taskDao()

    @Provides
    fun provideHabitDao(appDatabase: AppDatabase) = appDatabase.habitDao()

    @Provides
    fun provideWorkoutDao(appDatabase: AppDatabase) = appDatabase.workoutDao()

    @Provides
    fun provideHistoryTrackDao(appDatabase: AppDatabase) = appDatabase.historyTrackDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(
        app: Application
    ): SharedPreferences {
        return app.getSharedPreferences("shared_pref", MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideWorkManagerUtils(): WorkManagerUtils {
        return WorkManagerUtilsImpl()
    }

    @Provides
    fun provideWorkManager(
        @ApplicationContext app: Context
    ) = WorkManager.getInstance(app)

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): Preferences {
        return DataStorePreferences(context)
    }

    @Provides
    @Singleton
    fun provideFilterOutDigitsUseCase(): FilterOutDigits {
        return FilterOutDigits()
    }

    @Provides
    @Singleton
    fun provideAlarmHelper(@ApplicationContext context: Context): AlarmHelper {
        return AlarmHelperImpl(context)
    }

    @Provides
    @Singleton
    fun provideNetworkHelper(@ApplicationContext context: Context): NetworkHelper {
        return NetworkHelperImpl(context)
    }

    @HabitsAlarmReceiverClass
    @Provides
    fun provideAlarmReceiverClass(): Class<*> {
        return HabitsAlarmReceiver::class.java
    }

    @TasksAlarmReceiverClass
    @Provides
    fun provideTasksAlarmReceiverClass(): Class<*> {
        return TasksAlarmReceiver::class.java
    }

    @Local
    @Provides
    @Singleton
    fun providesSyncDataSourceLocal(
        historyTrackDao: HistoryTrackDao,
        taskDao: TaskDao,
        habitDao: HabitsDao,
        workoutDao: WorkoutDao
        ): SyncDataSource = SyncLocalDataSource(
        historyTrackDao,
        taskDao,
        habitDao,
        workoutDao
    )

    @Provides
    @Singleton
    fun provideSyncApi(client: OkHttpClient): SyncApi {
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
    fun providesSyncDataSourceRemote(
        syncApi: SyncApi
        ): SyncDataSource = SyncRemoteDataSource(
            syncApi
    )


    @Provides
    fun provideSyncRepository(
        @Remote remoteDataSource: SyncDataSource,
        @Local localDataSource: SyncDataSource,
        preferences: Preferences,
        networkHelper: NetworkHelper
    ): SyncRepository {
        return SyncRepositoryImpl(
            preferences = preferences,
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            networkHelper = networkHelper
        )
    }


    @Provides
    fun provideFeatureActionListener(
        repository: SyncRepository,
        tasksRepository: TasksRepository,
        habitsRepository: HabitsRepository,
        preferences: Preferences,
        networkHelper: NetworkHelper
    ): OperationListener {
        return OperationListener {
            SyncUtils.sync(
                repository,
                tasksRepository,
                habitsRepository,
                preferences,
                networkHelper
            ).isSuccess
        }
    }

}