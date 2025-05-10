package com.esteban.ruano.core_data.workManager.tasks

import androidx.work.ListenableWorker

interface CheckTasksWorker {
    suspend fun doWork(): ListenableWorker.Result
    fun checkTasks(): ListenableWorker.Result
}