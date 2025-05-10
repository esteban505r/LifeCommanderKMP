package com.esteban.ruano.core_data.workManager.tasks.factories

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.esteban.ruano.core.models.tasks.TaskResponseInterface


interface CheckTasksWorkerFactory {
    fun create(context: Context, workerParams: WorkerParameters,onSuccess:suspend (List<TaskResponseInterface>)->Unit
    ): ListenableWorker
}