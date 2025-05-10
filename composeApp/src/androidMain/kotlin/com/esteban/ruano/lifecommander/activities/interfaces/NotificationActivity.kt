package com.esteban.ruano.lifecommander.activities.interfaces

interface NotificationActivity {
    fun prepareNotifications(onNotificationsPrepared: () -> Unit)
}