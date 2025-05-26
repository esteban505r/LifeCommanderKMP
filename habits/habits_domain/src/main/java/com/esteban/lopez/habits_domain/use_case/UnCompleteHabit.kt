package com.esteban.ruano.habits_domain.use_case

import com.esteban.lopez.habits_domain.repository.HabitsRepository

class UnCompleteHabit(
    val repository: HabitsRepository
){

    suspend operator fun invoke(id: String): Result<Unit>{
        return repository.unCompleteHabit(id)
    }
}