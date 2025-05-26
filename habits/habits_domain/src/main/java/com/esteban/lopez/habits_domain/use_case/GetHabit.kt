package com.esteban.lopez.habits_domain.use_case

import com.esteban.lopez.habits_domain.repository.HabitsRepository
import com.lifecommander.models.Habit

class GetHabit(
    val repository: HabitsRepository
){
    suspend operator fun invoke(id:String,date:String): Result<Habit> {
        return repository.getHabit(id,date)
    }
}