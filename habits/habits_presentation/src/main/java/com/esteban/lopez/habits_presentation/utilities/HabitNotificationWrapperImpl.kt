package com.esteban.ruano.habits_presentation.utilities

import android.content.Context
import android.content.Intent
import android.util.Log
import com.esteban.ruano.core.models.habits.HabitNotificationWrapper
import com.esteban.ruano.core.models.habits.HabitResponseInterface
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

class HabitNotificationWrapperImpl : HabitNotificationWrapper {

    override fun scheduleHabitNotifications(
        habits: List<HabitResponseInterface>,
        intentClass: Class<*>,
        context: Context,
        setAlarmFunction: (hour: Int, minute: Int, intent: Intent) -> Unit
    ) {
        val now = LocalDateTime.now()

        habits.forEach { habit ->
            habit.dateTime?.toLocalDateTime()?.getTime()?.let { time ->
                val (hour, minute) = DateUIUtils.timeToIntPair(time)
                if (now.isFutureTime(hour, minute)) {
                    habit.reminders?.forEach { reminder ->
                        val minutes = reminder.time.toMinutes()
                        Log.d("HabitNotificationWrapper", "Checking habit ${habit.name} with reminder ${reminder.time}")
                        if (habit.dateTime?.toLocalDateTime()?.minusMinutes(minutes)?.isAfter(now) == true) {
                            Log.d("HabitNotificationWrapper", "Setting (reminder) alarm for habit ${habit.name}")
                            setHabitAlarm(
                                context,
                                intentClass,
                                habit,
                                reminder.time.toMinutes(),
                                setAlarmFunction
                            )
                        }
                    }
                    setHabitAlarm(context, intentClass, habit, 0, setAlarmFunction)
                }
            }
        }
    }

    override fun showNotifications(
        context: Context,
        habits: List<HabitResponseInterface>,
        showNotificationsFunction: (title: String, message: String) -> Unit
    ) {
        checkHabits(habits, {
            showNotificationsFunction(
                context.getString(R.string.habit_notification_title),
                context.getString(R.string.habit_notification_message)
            )
        }, {
            showNotificationsFunction(
                context.getString(R.string.due_habit_notification_title),
                context.getString(R.string.due_habit_notification_message, it.joinToString { habit -> habit.name?:"" })
            )
        })
    }

    override fun showNotification(
        context: Context,
        habit: HabitResponseInterface,
        showNotificationFunction: (id: Int, title: String, message: String) -> Unit
    ) {
        checkHabits(listOf(habit), {
            showNotificationFunction(
                NotificationsConstants.HABITS_NOTIFICATION_ID,
                context.getString(R.string.habit_reminder_notification_title),
                context.getString(R.string.habit_reminder_notification_title)
            )
        }, {
            showNotificationFunction(
                NotificationsConstants.HABITS_NOTIFICATION_ID,
                context.getString(R.string.due_habit_notification_title),
                context.getString(R.string.due_habit_notification_message, it.joinToString { habit -> habit.name?:"" })
            )
        })
    }

    private fun setHabitAlarm(
        context: Context,
        intentClass: Class<*>,
        habit: HabitResponseInterface,
        offsetMinutes: Long,
        setAlarmFunction: (hour: Int, minute: Int, intent: Intent) -> Unit
    ) {
        val customMessage = if(offsetMinutes>0){
            context.getString(R.string.habit_reminder_notification_message,
                context.getString(offsetMinutes.fromMinutesToMillis().toReminderType().toResource())
                ,habit.name)
        } else {
            context.getString(R.string.habit_name_notification_message, habit.name)
        }
        val customTitle = if(offsetMinutes>0){
            context.getString(R.string.habit_reminder_notification_title)
        } else {
            context.getString(R.string.habit_name_notification_title)
        }
        habit.dateTime?.toLocalDateTime()?.let { dueDateTime ->
            val adjustedTime = dueDateTime.minusMinutes(offsetMinutes)
            val (hour, minute) = DateUIUtils.timeToIntPair(adjustedTime.getTime())
            val intent = createBaseIntent(context, intentClass, habit,customTitle,customMessage)
            Log.d("HabitNotificationWrapper", "Setting alarm for habit ${habit.name}")
            setAlarmFunction(hour, minute, intent)
        }
    }

    private fun createBaseIntent(
         context: Context,
         intentClass: Class<*>,
         habit: HabitResponseInterface,
         customTitle:String,
         customMessage:String): Intent {
        return Intent(context, intentClass).apply {
            putExtra("id", habit.id)
            putExtra("title", customTitle)
            putExtra(
                "message",
                customMessage
            )
            putExtra("onClickPath", Routes.HABITS.name)
            putExtra("channelId", context.getString(R.string.habit_channel_id))
        }
    }

    private fun checkHabits(
        habits: List<HabitResponseInterface>,
        onPendingHabits: (List<HabitResponseInterface>) -> Unit,
        onDueHabits: (List<HabitResponseInterface>) -> Unit
    ) {
        val now = LocalDate.now()
        val pendingHabits = habits.filter { it.dateTime?.toLocalDateTime()?.toLocalDate() == now }
        val dueHabits = habits.filter { it.dateTime?.toLocalDateTime()?.toLocalDate()?.isBefore(now) == true }

        when {
            pendingHabits.isNotEmpty() -> onPendingHabits(pendingHabits)
            dueHabits.isNotEmpty() -> onDueHabits(dueHabits)
        }
    }
}
