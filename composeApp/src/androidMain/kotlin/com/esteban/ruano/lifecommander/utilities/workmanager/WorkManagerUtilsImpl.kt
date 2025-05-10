package com.esteban.ruano.lifecommander.utilities.workmanager

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.esteban.ruano.core_ui.WorkManagerUtils
import com.esteban.ruano.habits_data.workers.CheckHabitsWorkerImpl
import com.esteban.ruano.tasks_data.workers.CheckTasksWorkerImpl
import java.util.concurrent.TimeUnit

class WorkManagerUtilsImpl: WorkManagerUtils {

    override fun runWorkManagerTasks(workManager: WorkManager){

        val habitsWorkRequest = PeriodicWorkRequestBuilder<CheckHabitsWorkerImpl>(15, TimeUnit.MINUTES)
            .build()

        val tasksWorkRequest = PeriodicWorkRequestBuilder<CheckTasksWorkerImpl>(15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "${CheckHabitsWorkerImpl::class.java.simpleName}Worker",
            ExistingPeriodicWorkPolicy.KEEP,
            habitsWorkRequest)

        workManager.enqueueUniquePeriodicWork(
            "${CheckTasksWorkerImpl::class.java.simpleName}Worker",
            ExistingPeriodicWorkPolicy.KEEP,
            tasksWorkRequest)
    }

    override fun runHabitsTasks(workManager: WorkManager, restart: Boolean){
        val habitsWorkRequest = PeriodicWorkRequestBuilder<CheckHabitsWorkerImpl>(15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "${CheckHabitsWorkerImpl::class.java.simpleName}Worker",
            if(restart) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP,
            habitsWorkRequest)
    }

    override fun runTasksTasks(workManager: WorkManager, restart: Boolean) {
        val tasksWorkRequest = PeriodicWorkRequestBuilder<CheckTasksWorkerImpl>(15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "${CheckTasksWorkerImpl::class.java.simpleName}Worker",
            if(restart) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP,
            tasksWorkRequest)
    }


}