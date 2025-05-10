package com.esteban.ruano.habits_domain.use_case

import com.esteban.ruano.habits_domain.model.Habit
import com.esteban.ruano.habits_domain.repository.HabitsRepository

class AddHabit(
    val repository: HabitsRepository
){

    suspend operator fun invoke(habit: Habit): Result<Unit> {
        return repository.addHabit(habit)
    }
}