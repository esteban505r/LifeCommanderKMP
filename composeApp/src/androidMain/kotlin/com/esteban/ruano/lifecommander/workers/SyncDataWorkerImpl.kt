package com.esteban.ruano.lifecommander.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.habits_domain.repository.HabitsRepository
import com.esteban.ruano.lifecommander.database.SyncDTO
import com.esteban.ruano.lifecommander.domain.repository.SyncRepository
import com.esteban.ruano.lifecommander.utilities.SyncUtils
import com.esteban.ruano.tasks_domain.repository.TasksRepository
import com.esteban.ruano.workout_domain.repository.WorkoutRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking

class SyncDataWorkerImpl @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SyncRepository,
    private val tasksRepository: TasksRepository,
    private val habitsRepository: HabitsRepository,
    private val workoutRepository: WorkoutRepository,
    private val preferences: Preferences,
    private val networkHelper: NetworkHelper,
    private val onSuccess: (SyncDTO) -> Unit,
) : Worker(context, workerParams),
    SyncWorker {

    override fun doWork(): Result {
        return sync()
    }

    override fun sync(): Result {
        return runBlocking {
            SyncUtils.sync(
                repository = repository,
                tasksRepository = tasksRepository,
                habitsRepository = habitsRepository,
                preferences = preferences,
                networkHelper = networkHelper
            ).fold(
                onSuccess = {
                    onSuccess(it)
                    Result.success()
                },
                onFailure = {
                    Result.failure()
                }
            )
        }
    }
}


