package com.esteban.ruano.core_data.workManager.habits

import androidx.work.ListenableWorker

interface CheckHabitsWorker {
    suspend fun doWork(): ListenableWorker.Result
    fun checkHabits(): ListenableWorker.Result
}