package com.esteban.ruano.lifecommander

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.esteban.ruano.core_data.workManager.factories.CustomWorkerFactory
import com.esteban.ruano.core_ui.WorkManagerUtils
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LifeCommanderApp : Application() {

    @Inject
    lateinit var workerFactory: CustomWorkerFactory

    @Inject
    lateinit var workManagerUtils: WorkManagerUtils

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            Log.i("LifeCommanderApp", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("LifeCommanderApp", "Failed to initialize Firebase: ${e.message}")
        }

        WorkManager.initialize(
            this, Configuration.Builder().setWorkerFactory(
                workerFactory
            )
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()
        )

        val workManager = WorkManager.getInstance(this)

        //LogUtils.logToDownloadsFolderWithDateTime("Running WorkManager tasks from app")
        /*workManagerUtils.runWorkManagerTasks(workManager)
        workManagerUtils.runHabitsTasks(workManager)*/
    }
}