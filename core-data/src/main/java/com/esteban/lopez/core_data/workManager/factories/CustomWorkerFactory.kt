package com.esteban.ruano.core_data.workManager.factories

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.esteban.ruano.core.di.HabitsAlarmReceiverClass
import com.esteban.ruano.core.di.TasksAlarmReceiverClass
import com.esteban.ruano.core.di.TasksNotificationHelper
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NotificationsHelper
import com.esteban.ruano.core_data.helpers.AlarmHelper
import com.esteban.ruano.core.models.habits.HabitNotificationWrapper
import com.esteban.ruano.core.models.tasks.TasksNotificationWrapper
import com.esteban.ruano.core.utils.DateUtils.toLocalTime
import com.esteban.ruano.core.utils.NotificationsConstants
import com.esteban.ruano.core_data.workManager.habits.factories.CheckHabitsWorkerFactory
import com.esteban.ruano.core_data.workManager.tasks.factories.CheckTasksWorkerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import javax.inject.Inject

class CustomWorkerFactory @Inject constructor(
    private val checkHabitsWorkerFactory: CheckHabitsWorkerFactory,
    private val checkTasksWorkerFactory: CheckTasksWorkerFactory,
    private val habitNotificationWrapper: HabitNotificationWrapper,
    private val tasksNotificationWrapper: TasksNotificationWrapper,
    private val alarmHelper: AlarmHelper,
    private val preferences: Preferences,
    @TasksNotificationHelper private val tasksNotificationsHelper: NotificationsHelper,
    @HabitsAlarmReceiverClass private val habitsAlarmReceiverClass: Class<*>,
    @TasksAlarmReceiverClass private val tasksAlarmReceiverClass: Class<*>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Log.d("CustomWorkerFactory", "Worker class name: $workerClassName")
        return when (workerClassName) {
            "com.esteban.ruano.habits_data.workers.CheckHabitsWorkerImpl" ->
                checkHabitsWorkerFactory.create(appContext, workerParameters, onSuccess = {
                    val lastTime = preferences.loadLastHabitNotificationReminderTime().first()
                    if (LocalTime.now().minusMinutes(30).isAfter(lastTime.toLocalTime())) {
                        habitNotificationWrapper.showNotifications(
                            appContext,
                            it,
                            showNotificationsFunction = { title, message ->
                                val notification = tasksNotificationsHelper.createNotification(
                                    appContext,
                                    title,
                                    message,
                                    null
                                )
                                tasksNotificationsHelper.showNotification(
                                    appContext,
                                    NotificationsConstants.HABITS_NOTIFICATION_ID, notification
                                )
                                Log.d("CheckHabitsWorker", "Notification shown: $title - $message")
                            }
                        )
                        preferences.saveHabitLastNotificationReminderTime(LocalTime.now().toString())
                    }
                    habitNotificationWrapper.scheduleHabitNotifications(
                        it,
                        habitsAlarmReceiverClass,
                        appContext,
                        setAlarmFunction = { hour, minute, intent ->
                            alarmHelper.setAlarm(hour, minute, intent)
                            Log.d("CheckHabitsWorker", "Alarm set at $hour:$minute")
                        }
                    )
                })

            "com.esteban.ruano.tasks_data.workers.CheckTasksWorkerImpl" ->
                checkTasksWorkerFactory.create(appContext, workerParameters, onSuccess = {
                    val lastTime = preferences.loadLastTaskNotificationReminderTime().first()
                    if (LocalTime.now().minusMinutes(30).isAfter(lastTime.toLocalTime())) {
                        tasksNotificationWrapper.showNotifications(
                            appContext,
                            it,
                            showNotificationsFunction = { title, message ->
                                val notification = tasksNotificationsHelper.createNotification(
                                    appContext,
                                    title,
                                    message,
                                    null
                                )
                                tasksNotificationsHelper.showNotification(
                                    appContext,
                                    NotificationsConstants.TASKS_NOTIFICATION_ID, notification
                                )
                                Log.d("CheckTasksWorker", "Notification shown: $title - $message")
                            }
                        )
                        preferences.saveTaskLastNotificationReminderTime(LocalTime.now().toString())
                    }
                    tasksNotificationWrapper.scheduleTasksNotifications(
                        appContext,
                        it,
                        tasksAlarmReceiverClass,
                        setAlarmFunction = { hour, minute, intent ->
                            alarmHelper.setAlarm(hour, minute, intent)
                            Log.d("CheckTasksRemindersWorker", "Alarm set at $hour:$minute")
                        }
                    )
                })

            else ->
                // Return null, so the base class can delegate to the default WorkerFactory.
                null
        }
    }
}