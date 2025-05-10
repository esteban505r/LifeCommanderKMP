package com.esteban.ruano.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.esteban.ruano.core.utils.DateUtils.toLocalDate
import com.esteban.ruano.core.utils.DateUtils.toLocalDateTime
import java.io.File
import java.io.IOException

object LogUtils{
    fun logToDownloadsFolder(logMessage: String) {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val logFile = File(downloadsDirectory, "appLog.txt")
        try {
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            logFile.appendText("$logMessage\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun logToDownloadsFolderWithDateTime(logMessage: String) {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val logFile = File(downloadsDirectory, "appLog.txt")
        try {
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            logFile.appendText("$logMessage - ${System.currentTimeMillis().toLocalDateTime().plusHours(5)}\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun checkPermission(context: Context): Boolean {
        return with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with false
            }
            true
        }
    }
}