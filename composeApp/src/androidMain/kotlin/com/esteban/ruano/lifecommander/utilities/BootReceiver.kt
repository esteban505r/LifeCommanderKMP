package com.esteban.ruano.lifecommander.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.esteban.ruano.core.utils.LogUtils
import com.esteban.ruano.core_ui.WorkManagerUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var workManagerUtils: WorkManagerUtils

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            LogUtils.logToDownloadsFolderWithDateTime( "Boot completed")
            LogUtils.logToDownloadsFolderWithDateTime("Running WorkManager tasks from bootReceiver")
            workManagerUtils.runWorkManagerTasks(WorkManager.getInstance(context))
        }
    }
}