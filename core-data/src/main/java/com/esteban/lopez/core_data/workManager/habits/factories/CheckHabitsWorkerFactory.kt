package com.esteban.ruano.core_data.workManager.habits.factories

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.esteban.ruano.core.models.habits.HabitResponseInterface


interface CheckHabitsWorkerFactory {
    fun create(context: Context, workerParams: WorkerParameters,onSuccess:suspend (List<HabitResponseInterface>)->Unit
    ): ListenableWorker
}