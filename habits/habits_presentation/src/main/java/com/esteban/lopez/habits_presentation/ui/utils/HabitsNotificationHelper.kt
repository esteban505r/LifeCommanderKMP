package com.esteban.ruano.habits_presentation.ui.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.esteban.ruano.core.helpers.NotificationsHelper
import com.esteban.ruano.core_ui.R

class HabitsNotificationHelperImpl: NotificationsHelper {
    override fun createChannel(context: Context): NotificationChannel {
        val name = context.getString(R.string.habits_channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(context.getString(R.string.habit_channel_id), name, importance)
        mChannel.description = descriptionText
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
        return mChannel
    }

    override fun createNotification(
        context: Context,
        title: String,
        message: String,
        contentIntent: PendingIntent?
    ): Notification {
        createChannel(context)
        return NotificationCompat.Builder(context, context.getString(R.string.habit_channel_id))
            .setSmallIcon(R.drawable.baseline_check_circle_24)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    override fun checkPermission(context: Context): Boolean {
        return with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with false
            }
                true
        }
    }

    override fun showNotification(context: Context, id:Int, notification: Notification,) {
        with(NotificationManagerCompat.from(context)) {
            val permission = checkPermission(context)
            if (permission) {
                notify(id, notification)
            }
        }
    }



}