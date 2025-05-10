package com.esteban.ruano.core.models.habits

import android.content.Context
import android.content.Intent
import com.esteban.ruano.core.models.tasks.TaskResponseInterface

interface HabitNotificationWrapper{
fun scheduleHabitNotifications(
    habits: List<HabitResponseInterface>,
    intentClass: Class<*>,
    context: Context,
    setAlarmFunction: (hour: Int, minute: Int, intent: Intent) -> Unit
    )

fun showNotifications(
    context: Context,
    habits: List<HabitResponseInterface>,
    showNotificationsFunction: (title: String, message: String, ) -> Unit
)

fun showNotification(
    context: Context,
    task: HabitResponseInterface,
    showNotificationFunction: (id:Int,title: String, message: String, ) -> Unit
    )
}