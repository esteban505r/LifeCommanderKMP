package com.esteban.lopez.habits_domain.use_case

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.lopez.habits_domain.repository.HabitsRepository
import com.lifecommander.models.Habit

class GetHabitsByRangeDate(
    val repository: HabitsRepository
) {
    suspend operator fun invoke(
        filter: String? = null,
        page: Int? = null,
        limit: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        date: String? = null
    ): Result<List<Habit>> {
        if(startDate != null && endDate != null) {
            return repository.getHabitsByDateRange(
                filter = filter,
                page = page,
                limit = limit,
                startDate = startDate,
                endDate = endDate
            )
        }
        if(date != null) {
            return repository.getHabits(
                filter = filter,
                page = page,
                limit = limit,
                date = date
            )
        }

        return Result.failure(DataException.Network.Unknown)
    }
}