package com.esteban.ruano.habits_presentation.di

import com.esteban.ruano.core.di.HabitNotificationHelper
import com.esteban.ruano.core.helpers.NotificationsHelper
import com.esteban.ruano.core.models.habits.HabitNotificationWrapper
import com.esteban.ruano.habits_presentation.ui.utils.HabitsNotificationHelperImpl
import com.esteban.ruano.habits_presentation.utilities.HabitNotificationWrapperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class HabitPresentationModule {
    @Provides
    fun provideHabitNotificationHelper(): HabitNotificationWrapper {
        return HabitNotificationWrapperImpl()
    }

    @HabitNotificationHelper
    @Provides
    fun provideHabitsNotificationHelper(): NotificationsHelper {
        return HabitsNotificationHelperImpl()
    }
}