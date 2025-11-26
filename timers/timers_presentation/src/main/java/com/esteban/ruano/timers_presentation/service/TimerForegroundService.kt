package com.esteban.ruano.timers_presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.timers_presentation.service.TimerForegroundService.Companion.ACTION_START
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var updateJob: Job? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "timer_foreground_service"
        private const val CHANNEL_NAME = "Timer Service"
        private const val CHANNEL_DESCRIPTION = "Shows active timers in the notification bar"
        private const val ACTION_START = "com.esteban.ruano.timers.START"
        private const val ACTION_STOP = "com.esteban.ruano.timers.STOP"

        private var instance: TimerForegroundService? = null

        fun startService(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }

        // Utility for safely accessing instance
        fun getInstance(): TimerForegroundService? = instance

        // Static method to update timers
        fun updateTimers(timers: List<Timer>, lists: List<TimerList>) {
            instance?.updateTimers(timers, lists)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, createNotification(currentTimers))
                startUpdateLoop()
            }
            ACTION_STOP -> {
                stopForeground(true)
                stopSelf()
                updateJob?.cancel()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        updateJob?.cancel()
        serviceScope.cancel()
    }

    // Store current timer state
    private var currentTimers: List<Timer> = emptyList()
    private var currentLists: List<TimerList> = emptyList()

    // This function is used to update the timers when the data is received
    fun updateTimers(timers: List<Timer>, lists: List<TimerList>) {
        currentTimers = timers.filter { it.state == "RUNNING" || it.state == "PAUSED" }
        currentLists = lists
        updateNotification(currentTimers, currentLists)
    }

    // Starts the loop that updates the notification every second
    private fun startUpdateLoop() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                // Update notification with current timer state
                updateNotification(currentTimers, currentLists)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(timers: List<Timer>, lists: List<TimerList> = emptyList()): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getNotificationTitle(timers))
            .setContentText(getNotificationText(timers))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setShowWhen(false)

        // Add style for multiple timers
        if (timers.isNotEmpty()) {
            val style = NotificationCompat.InboxStyle()
            timers.forEach { timer ->
                val timeText = formatTime(timer.remainingSeconds)
                val listName = lists.find { it.timers?.any { t -> t.id == timer.id } == true }?.name ?: "Unknown"
                val status = if (timer.state == "PAUSED") "⏸ " else ""
                style.addLine("$status$listName: ${timer.name} - $timeText")
            }
            notificationBuilder.setStyle(style)
        }

        return notificationBuilder.build()
    }

    private fun getNotificationTitle(timers: List<Timer>): String {
        return when {
            timers.isEmpty() -> "No active timers"
            timers.size == 1 -> timers.first().name
            else -> "${timers.size} active timers"
        }
    }

    private fun getNotificationText(timers: List<Timer>): String {
        return if (timers.isEmpty()) {
            "Tap to manage timers"
        } else {
            timers.joinToString(", ") { timer ->
                "${timer.name}: ${formatTime(timer.remainingSeconds)}"
            }
        }
    }

    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%d:%02d", minutes, secs)
        }
    }

    private fun updateNotification(timers: List<Timer>, lists: List<TimerList>) {
        val notification = createNotification(timers, lists)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}


