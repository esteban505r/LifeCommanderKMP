package com.esteban.ruano.habits_domain.use_case

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.habits_domain.model.Habit
import com.esteban.ruano.habits_domain.repository.HabitsRepository

class UpdateHabit(
    val repository: HabitsRepository
){

    suspend operator fun invoke(id:String, habit: Habit): Result<Unit>{
        return repository.updateHabit(id,habit)
    }
}