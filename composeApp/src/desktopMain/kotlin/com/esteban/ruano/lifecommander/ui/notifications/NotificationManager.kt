package ui.notifications

import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class NotificationManager(
    private val trayState: TrayState,
    private val coroutineScope: CoroutineScope
) {
    fun sendNotification(title: String, message: String) {
        coroutineScope.launch {
            println("Sending notification: $title - $message")
            trayState.sendNotification(Notification(title, message, Notification.Type.Info))
        }
    }

    fun sendWarningNotification(title: String, message: String) {
        coroutineScope.launch {
            trayState.sendNotification(Notification(title, message, Notification.Type.Warning))
        }
    }

    fun updateTimerFile(message: String) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(System.getProperty("user.home"), ".config/LifeCommanderDesktop/timer")
                file.writeText(message)
            }
        }
    }
} 