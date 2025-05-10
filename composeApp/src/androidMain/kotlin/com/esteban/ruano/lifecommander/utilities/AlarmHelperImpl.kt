package com.esteban.ruano.lifecommander.utilities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.esteban.ruano.core_data.helpers.AlarmHelper
import java.lang.Exception
import java.util.Calendar


class AlarmHelperImpl(
    private val  context: Context
): AlarmHelper {

    private val TAG = "AlarmManagerUtils"

    override fun setAlarm(hour: Int, minute: Int, intent : Intent){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val uniqueId = "$hour-$minute-${intent.action}".hashCode()

        val pendingIntent = PendingIntent.getBroadcast(context, uniqueId, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
               alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        ){
            Log.e("AlarmManagerUtils", "Setting Alarm at ${calendar.time}")
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent), pendingIntent)
        }
        else{
            Log.e("AlarmManagerUtils", "It is not possible to set exact alarms")
        }
    }

    override fun cancelAllAlarms(){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val updateServiceIntent = Intent(
            context,
            HabitsAlarmReceiver::class.java
        )
        val pendingUpdateIntent = PendingIntent.getService(context, 0, updateServiceIntent,
            PendingIntent.FLAG_IMMUTABLE)
        try {
            alarmManager.cancel(pendingUpdateIntent)
        } catch (e: Exception) {
            Log.e(TAG, "AlarmManager update was not canceled. $e")
        }
    }
}