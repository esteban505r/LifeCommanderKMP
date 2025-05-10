package com.esteban.ruano.habits_domain.di

import com.esteban.ruano.habits_domain.repository.HabitsRepository
import com.esteban.ruano.habits_domain.use_case.AddHabit
import com.esteban.ruano.habits_domain.use_case.CompleteHabit
import com.esteban.ruano.habits_domain.use_case.DeleteHabit
import com.esteban.ruano.habits_domain.use_case.GetHabit
import com.esteban.ruano.habits_domain.use_case.GetHabitsByRangeDate
import com.esteban.ruano.habits_domain.use_case.GetHabits
import com.esteban.ruano.habits_domain.use_case.HabitUseCases
import com.esteban.ruano.habits_domain.use_case.UnCompleteHabit
import com.esteban.ruano.habits_domain.use_case.UpdateHabit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object HabitsDomainModule {

    @ViewModelScoped
    @Provides
    fun provideHabitUseCases(
        repository: HabitsRepository
    ): HabitUseCases {
        return HabitUseCases(
            getHabit = GetHabit(repository),
            getHabits = GetHabits(repository),
            addHabit = AddHabit(repository),
            completeHabit = CompleteHabit(repository),
            deleteHabit = DeleteHabit(repository),
            getHabitsByRangeDate = GetHabitsByRangeDate(repository),
            unCompleteHabit = UnCompleteHabit(repository),
            updateHabit = UpdateHabit(repository)
        )
    }
}