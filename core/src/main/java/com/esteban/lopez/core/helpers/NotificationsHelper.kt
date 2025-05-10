package com.esteban.ruano.core.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context

interface NotificationsHelper{
    fun showNotification(
        context:Context,
        id:Int,
        notification: Notification
    )

    fun createNotification(
        context: Context,
        title: String,
        message: String,
        contentIntent: PendingIntent?): Notification

    fun checkPermission(context: Context): Boolean

    fun createChannel(context: Context): NotificationChannel
}