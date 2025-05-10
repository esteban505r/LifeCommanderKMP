package com.esteban.ruano.habits_data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.esteban.ruano.core.utils.DateUtils.parseDate
import com.esteban.ruano.core_data.workManager.habits.CheckHabitsWorker
import com.esteban.ruano.habits_data.mapper.toResponseModel
import com.esteban.ruano.habits_data.remote.dto.HabitResponse
import com.esteban.ruano.habits_domain.repository.HabitsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class
CheckHabitsWorkerImpl @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HabitsRepository,
    private val onSuccess: suspend (List<HabitResponse>) -> Unit,) : CoroutineWorker(context, workerParams),
    CheckHabitsWorker {

    override suspend fun doWork(): Result {
        return checkHabits()
    }

    override fun checkHabits(): Result {
        return runBlocking {
            val response = repository.getHabits(
                filter = null,
                limit = 30,
                page = null,
                date = LocalDate.now().parseDate()
            )
            if(response.isSuccess) {
                val habits = response.getOrDefault(listOf())
                onSuccess(habits.map {
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


