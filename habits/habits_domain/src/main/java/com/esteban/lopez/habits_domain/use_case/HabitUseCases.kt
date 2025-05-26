package com.esteban.ruano.habits_domain.use_case

import com.esteban.lopez.habits_domain.use_case.GetHabit
import com.esteban.lopez.habits_domain.use_case.GetHabitsByRangeDate

data class HabitUseCases(
    val getHabitsByRangeDate: GetHabitsByRangeDate,
    val getHabits: GetHabits,
    val deleteHabit: DeleteHabit,
    val completeHabit: CompleteHabit,
    val unCompleteHabit: UnCompleteHabit,
    val addHabit: AddHabit,
    val updateHabit: UpdateHabit,
    val getHabit: GetHabit
)
