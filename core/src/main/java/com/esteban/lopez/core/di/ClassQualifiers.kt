package com.esteban.ruano.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HabitsAlarmReceiverClass

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TasksAlarmReceiverClass

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TasksNotificationHelper

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HabitNotificationHelper