package com.esteban.ruano.habits_domain.use_case

import com.esteban.lopez.habits_domain.repository.HabitsRepository
import com.lifecommander.models.Habit

class GetHabits(
    val repository: HabitsRepository
) {
    suspend operator fun invoke(
        filter: String? = null,
        page: Int? = null,
        limit: Int? = null,
        date: String
    ): Result<List<Habit>> {
        return repository.getHabits(
            filter = filter,
            page = page,
            limit = limit,
            date = date
        )
    }
}