package com.esteban.ruano.core.models.tasks

import android.content.Context
import android.content.Intent

interface TasksNotificationWrapper{
fun scheduleTasksNotifications(
    context: Context,
    tasks: List<TaskResponseInterface>,
    intentClass: Class<*>,
    setAlarmFunction: (hour: Int, minute: Int, intent: Intent) -> Unit
    )

fun showNotifications(
    context: Context,
    habits: List<TaskResponseInterface>,
    showNotificationsFunction: (title: String, message: String, ) -> Unit
)

fun showNotification(
    context: Context,
    task: TaskResponseInterface,
    showNotificationFunction: (id:Int,title: String, message: String, ) -> Unit
)

}