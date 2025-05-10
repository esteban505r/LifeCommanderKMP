package com.esteban.ruano.core_data.helpers

import android.content.Intent

interface AlarmHelper {
    fun setAlarm(hour: Int, minute: Int, intent : Intent)
    fun cancelAllAlarms()
}