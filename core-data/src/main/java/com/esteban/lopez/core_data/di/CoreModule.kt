package com.esteban.ruano.core_data.di

import android.content.Context
import com.esteban.ruano.core.di.HabitsAlarmReceiverClass
import com.esteban.ruano.core.di.TasksAlarmReceiverClass
import com.esteban.ruano.core.di.TasksNotificationHelper
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NotificationsHelper
import com.esteban.ruano.core.models.habits.HabitNotificationWrapper
import com.esteban.ruano.core.models.tasks.TasksNotificationWrapper
import com.esteban.ruano.core_data.di.interceptor.AuthInterceptor
import com.esteban.ruano.core_data.helpers.AlarmHelper
import com.esteban.ruano.core_data.workManager.factories.CustomWorkerFactory
import com.esteban.ruano.core_data.workManager.habits.factories.CheckHabitsWorkerFactory
import com.esteban.ruano.core_data.workManager.tasks.factories.CheckTasksWorkerFactory
import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        @ApplicationContext context: Context
    ): AuthInterceptor {
        return AuthInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideCustomWorkerFactory(
        checkHabitsWorkerFactory: CheckHabitsWorkerFactory,
        habitNotificationWrapper: HabitNotificationWrapper,
        alarmHelper: AlarmHelper,
        tasksNotificationWrapper: TasksNotificationWrapper,
        checkTasksWorkerFactory: CheckTasksWorkerFactory,
        @HabitsAlarmReceiverClass habitsAlarmReceiverClass: Class<*>,
        @TasksAlarmReceiverClass tasksAlarmReceiverClass: Class<*>,
        @TasksNotificationHelper tasksNotificationHelper: NotificationsHelper,
        preferences: Preferences
    ): CustomWorkerFactory {
        return CustomWorkerFactory(
            checkHabitsWorkerFactory = checkHabitsWorkerFactory,
            habitNotificationWrapper = habitNotificationWrapper,
            alarmHelper = alarmHelper,
            tasksNotificationWrapper = tasksNotificationWrapper,
            checkTasksWorkerFactory = checkTasksWorkerFactory,
            tasksNotificationsHelper = tasksNotificationHelper,
            habitsAlarmReceiverClass = habitsAlarmReceiverClass,
            tasksAlarmReceiverClass = tasksAlarmReceiverClass,
            preferences = preferences
        )
    }
}