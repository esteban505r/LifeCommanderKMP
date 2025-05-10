package com.esteban.ruano.habits_domain.use_case

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.habits_domain.model.Habit
import com.esteban.ruano.habits_domain.repository.HabitsRepository

class GetHabit(
    val repository: HabitsRepository
){
    suspend operator fun invoke(id:String,date:String): Result<Habit> {
        return repository.getHabit(id,date)
    }
}