package com.esteban.ruano.habits_domain.use_case

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.habits_domain.repository.HabitsRepository

class CompleteHabit(
    val repository: HabitsRepository
){

    suspend operator fun invoke(id: String): Result<Unit>{
        return repository.completeHabit(id)
    }
}