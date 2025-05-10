package com.esteban.ruano.core.utils.devices

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat.startActivity

object DeviceUtilities {
    fun prepareAutoStartInXiaomi(context: Context){
        val manufacturer = "xiaomi"
        if (manufacturer.equals(Build.MANUFACTURER, ignoreCase = true)) {
            val intent = Intent()
            intent.setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            )
            context.startActivity(intent)
        }
    }
}