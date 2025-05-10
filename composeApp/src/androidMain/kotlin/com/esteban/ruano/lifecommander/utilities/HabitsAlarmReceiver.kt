package com.esteban.ruano.lifecommander.utilities


import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.esteban.ruano.core.di.HabitNotificationHelper
import com.esteban.ruano.core.helpers.NotificationsHelper
import com.esteban.ruano.lifecommander.activities.MainActivity
import com.esteban.ruano.habits_presentation.ui.utils.HabitsNotificationHelperImpl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HabitsAlarmReceiver : BroadcastReceiver() {


    @Inject
    @HabitNotificationHelper
    lateinit var helper: NotificationsHelper
    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     *
     * @throws SecurityException if you try to call this method without the necessary permissions.
     *
     * Note: Do not call this method directly without first checking and asking for the necessary permissions.
     */
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d("HabitsAlarmReceiver", "onReceive")
        context?.let {
            val title = intent?.getStringExtra("title")
            val message = intent?.getStringExtra("message")
            val id = intent?.getStringExtra("id")
            val onClickPath = intent?.getStringExtra("onClickPath")
            val channelId = intent?.getStringExtra("channelId")

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = "android.intent.action.MAIN"
                putExtra("navigate_to_screen" , onClickPath)
            }

            val uniqueId = id?.hashCode() ?: 0

            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, uniqueId, intent, PendingIntent.FLAG_IMMUTABLE)

            /*val notification = NotificationUtils.createNotification(
                context,
                "Task Reminder",
                "You need to do $title right now",
                pendingIntent
            )*/
            title?.let {
                val notification = helper.createNotification(
                    context,
                    title,
                    message ?: "",
                    pendingIntent
                )

                helper.showNotification(context,  uniqueId,notification)
            }


        }
    }
}