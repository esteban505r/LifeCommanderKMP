package com.esteban.ruano.habits_domain.use_case

import com.esteban.lopez.habits_domain.repository.HabitsRepository

class DeleteHabit(
    val repository: HabitsRepository
){
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.deleteHabit(id)
    }
}