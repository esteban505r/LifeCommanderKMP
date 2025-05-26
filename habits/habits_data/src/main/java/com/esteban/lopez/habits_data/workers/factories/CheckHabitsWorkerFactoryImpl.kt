package com.esteban.ruano.habits_data.workers.factories

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.esteban.ruano.core.models.habits.HabitResponseInterface
import com.esteban.ruano.core_data.workManager.habits.factories.CheckHabitsWorkerFactory
import com.esteban.ruano.habits_data.workers.CheckHabitsWorkerImpl
import com.esteban.lopez.habits_domain.repository.HabitsRepository
import javax.inject.Inject

class CheckHabitsWorkerFactoryImpl @Inject constructor(
    private val repository: HabitsRepository,
) : CheckHabitsWorkerFactory {

    override fun create(
        context: Context,
        workerParams: WorkerParameters,
        onSuccess: suspend (List<HabitResponseInterface>) -> Unit
    ): ListenableWorker {
        return CheckHabitsWorkerImpl(context, workerParams, repository, onSuccess)
    }
}