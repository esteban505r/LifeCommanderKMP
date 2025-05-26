package com.esteban.ruano.tasks_data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.esteban.ruano.core_data.workManager.habits.CheckHabitsWorker
import com.esteban.ruano.core_data.workManager.tasks.CheckTasksWorker
import com.esteban.ruano.tasks_data.remote.model.TaskResponse
import com.esteban.ruano.tasks_data.mappers.toResponseModel
import com.esteban.ruano.lifecommander.models.TaskFilters
import com.esteban.ruano.tasks_domain.repository.TasksRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking

class CheckTasksWorkerImpl @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TasksRepository,
    private val onSuccess: suspend (List<TaskResponse>) -> Unit,)  : CoroutineWorker(context, workerParams),
    CheckTasksWorker {

    override suspend fun doWork(): Result {
        return checkTasks()
    }

    override fun checkTasks(): Result {
        return runBlocking {
            val today = TaskFilters.TODAY.getDateRangeByFilter()
            val response = repository.getTasksByDateRange(
                filter = null,
                limit = 30,
                page = null,
                startDate = today.first!!,
                endDate = today.second!!
            )
            if(response.isSuccess) {
                val tasks = response.getOrDefault(listOf())
                onSuccess(tasks.map {
                    it.toResponseModel()
                })
                Result.success()
            }
            else{
                Result.failure()
            }
        }
    }
}


