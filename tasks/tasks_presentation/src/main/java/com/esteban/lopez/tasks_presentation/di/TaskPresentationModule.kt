package com.esteban.ruano.tasks_presentation.di

import com.esteban.ruano.core.di.TasksNotificationHelper
import com.esteban.ruano.core.helpers.NotificationsHelper
import com.esteban.ruano.core.models.tasks.TasksNotificationWrapper
import com.esteban.ruano.tasks_presentation.ui.utils.TasksNotificationHelperImpl
import com.esteban.ruano.tasks_presentation.utils.TaskNotificationWrapperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class TaskPresentationModule {
    @Provides
    fun provideHabitNotificationHelper(): TasksNotificationWrapper {
        return TaskNotificationWrapperImpl()
    }

    @TasksNotificationHelper
    @Provides
    fun provideTaskNotificationHelper(): NotificationsHelper{
        return TasksNotificationHelperImpl()
    }
}