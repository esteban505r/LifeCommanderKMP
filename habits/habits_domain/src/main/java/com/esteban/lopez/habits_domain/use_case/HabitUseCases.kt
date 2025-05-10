package com.esteban.ruano.habits_domain.use_case

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
