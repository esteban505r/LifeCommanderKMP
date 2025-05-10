package com.esteban.ruano.core_ui

import androidx.work.WorkManager

interface WorkManagerUtils {
    fun runWorkManagerTasks(workManager: WorkManager)
    fun runHabitsTasks(workManager: WorkManager, restart: Boolean = false)
    fun runTasksTasks(workManager: WorkManager, restart: Boolean = false)
}