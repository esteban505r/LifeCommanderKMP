package com.esteban.ruano.habits_domain.use_case

import com.esteban.lopez.habits_domain.repository.HabitsRepository
import com.lifecommander.models.Habit

class AddHabit(
    val repository: HabitsRepository
){

    suspend operator fun invoke(habit: Habit): Result<Unit> {
        return repository.addHabit(habit)
    }
}