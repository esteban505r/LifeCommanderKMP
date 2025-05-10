package com.esteban.ruano.tasks_presentation.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.esteban.ruano.core.models.tasks.TaskResponseInterface
import com.esteban.ruano.core.models.tasks.TasksNotificationWrapper
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.core.utils.NotificationsConstants
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.utils.DateUIUtils
import com.esteban.ruano.core_ui.utils.DateUIUtils.fromMinutesToMillis
import com.esteban.ruano.core_ui.utils.DateUIUtils.getTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.isFutureTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.toMinutes
import com.esteban.ruano.core_ui.utils.ReminderType.Companion.toReminderType
import com.esteban.ruano.core_ui.utils.ReminderType.Companion.toResource
import java.time.LocalDate
import java.time.LocalDateTime

class TaskNotificationWrapperImpl : TasksNotificationWrapper {

    override fun scheduleTasksNotifications(
        context: Context,
        tasks: List<TaskResponseInterface>,
        intentClass: Class<*>,
        setAlarmFunction: (hour: Int, minute: Int, intent: Intent) -> Unit
    ) {
        val now = LocalDateTime.now()

        tasks.forEach { task ->
            task.dueDateTime?.toLocalDateTime()?.getTime()?.let { time ->
                val (hour, minute) = DateUIUtils.timeToIntPair(time)
                if (now.isFutureTime(hour, minute) && task.done == false) {
                    task.reminders?.forEach { reminder ->
                        Log.d("TaskNotificationWrapper", "Setting (reminder) alarm for task ${task.name}")
                        val minutes = reminder.time.toMinutes()
                        if (task.dueDateTime?.toLocalDateTime()?.minusMinutes(minutes)?.isAfter(now) == true){
                            setTaskAlarm(context, intentClass, task, minutes, setAlarmFunction)
                        }
                    }
                    setTaskAlarm(context, intentClass, task, 0, setAlarmFunction)
                }
            }
        }
    }

    override fun showNotifications(
        context: Context,
        tasks: List<TaskResponseInterface>,
        showNotificationsFunction: (title: String, message: String) -> Unit
    ) {
        checkTasks(tasks, {
            showNotificationsFunction(
                context.getString(R.string.task_notification_title),
                context.getString(R.string.task_notification_message)
            )
        }, {
            showNotificationsFunction(
                context.getString(R.string.due_task_notification_title),
                context.getString(R.string.due_task_notification_message)
            )
        })
    }

    override fun showNotification(
        context: Context,
        task: TaskResponseInterface,
        showNotificationFunction: (id: Int, title: String, message: String) -> Unit
    ) {
        checkTasks(listOf(task), {
            showNotificationFunction(
                NotificationsConstants.TASKS_NOTIFICATION_ID,
                context.getString(R.string.task_reminder_notification_title),
                context.getString(R.string.task_reminder_notification_message)
            )
        }, {
            showNotificationFunction(
                NotificationsConstants.TASKS_NOTIFICATION_ID,
                context.getString(R.string.due_task_notification_title),
                context.getString(R.string.due_task_notification_message)
            )
        })
    }

    private fun setTaskAlarm(
        context: Context,
        intentClass: Class<*>,
        task: TaskResponseInterface,
        offsetMinutes: Long,
        setAlarmFunction: (hour: Int, minute: Int, intent: Intent) -> Unit
    ) {
        val customMessage = if(offsetMinutes>0){
            context.getString(R.string.task_reminder_notification_message,
            context.getString(offsetMinutes.fromMinutesToMillis().toReminderType().toResource())
                ,task.name)
        } else {
            context.getString(R.string.task_name_notification_message, task.name)
        }
        val customTitle = if(offsetMinutes>0){
            context.getString(R.string.task_reminder_notification_title)
        } else {
            context.getString(R.string.task_name_notification_title)
        }
        task.dueDateTime?.toLocalDateTime()?.let { dueDateTime ->
            val adjustedTime = dueDateTime.minusMinutes(offsetMinutes)
            val (hour, minute) = DateUIUtils.timeToIntPair(adjustedTime.getTime())
            val intent = createBaseIntent(context, intentClass, task, customTitle,customMessage)
            Log.d("TaskNotificationWrapper", "Setting alarm for task ${task.name}")
            setAlarmFunction(hour, minute, intent)
        }
    }

    private fun createBaseIntent(context: Context, intentClass: Class<*>, task: TaskResponseInterface,customTitle:String, customMessage:String): Intent {
        return Intent(context, intentClass).apply {
            putExtra("id", task.id)
            putExtra("title", customTitle)
            putExtra(
                "message",
                customMessage
            )
            putExtra("onClickPath", Routes.BASE.TASKS.name)
            putExtra("channelId", context.getString(R.string.task_channel_id))
        }
    }

    private fun checkTasks(
        tasks: List<TaskResponseInterface>,
        onPendingTasks: (List<TaskResponseInterface>) -> Unit,
        onDueTasks: (List<TaskResponseInterface>) -> Unit
    ) {
        val now = LocalDate.now()
        val pendingTasks = tasks.filter { it.dueDateTime?.toLocalDateTime()?.toLocalDate() == now }
        val dueTasks = tasks.filter { it.dueDateTime?.toLocalDateTime()?.toLocalDate()?.isBefore(now) == true }

        when {
            pendingTasks.isNotEmpty() -> onPendingTasks(pendingTasks)
            dueTasks.isNotEmpty() -> onDueTasks(dueTasks)
        }
    }
}
