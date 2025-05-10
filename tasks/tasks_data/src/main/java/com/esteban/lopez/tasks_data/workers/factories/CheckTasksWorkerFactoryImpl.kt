package com.esteban.ruano.tasks_data.workers.factories

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.esteban.ruano.core.models.tasks.TaskResponseInterface
import com.esteban.ruano.core_data.workManager.tasks.factories.CheckTasksWorkerFactory
import com.esteban.ruano.tasks_data.workers.CheckTasksWorkerImpl
import com.esteban.ruano.tasks_domain.repository.TasksRepository
import javax.inject.Inject

class CheckTasksWorkerFactoryImpl @Inject constructor(
    private val repository: TasksRepository,
) : CheckTasksWorkerFactory {

    override fun create(
        context: Context,
        workerParams: WorkerParameters,
        onSuccess: suspend (List<TaskResponseInterface>) -> Unit
    ): ListenableWorker {
        return CheckTasksWorkerImpl(context, workerParams, repository, onSuccess)
    }
}