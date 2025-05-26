package com.esteban.ruano.habits_domain.use_case

import com.esteban.lopez.habits_domain.repository.HabitsRepository
import com.lifecommander.models.Habit

class UpdateHabit(
    val repository: HabitsRepository
){

    suspend operator fun invoke(id:String, habit: Habit): Result<Unit>{
        return repository.updateHabit(id,habit)
    }
}