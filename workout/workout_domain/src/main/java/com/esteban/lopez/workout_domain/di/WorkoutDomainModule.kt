package com.esteban.ruano.workout_domain.di

import com.esteban.ruano.workout_domain.repository.WorkoutRepository
import com.esteban.ruano.workout_domain.use_cases.AddSet
import com.esteban.ruano.workout_domain.use_cases.GetExerciseById
import com.esteban.ruano.workout_domain.use_cases.GetExercises
import com.esteban.ruano.workout_domain.use_cases.GetExercisesByWorkoutDay
import com.esteban.ruano.workout_domain.use_cases.GetWorkoutDashboard
import com.esteban.ruano.workout_domain.use_cases.GetWorkoutDayById
import com.esteban.ruano.workout_domain.use_cases.GetWorkoutDayByNumber
import com.esteban.ruano.workout_domain.use_cases.GetWorkoutDayStatus
import com.esteban.ruano.workout_domain.use_cases.GetWorkoutDays
import com.esteban.ruano.workout_domain.use_cases.GetWorkoutDaysWithExercises
import com.esteban.ruano.workout_domain.use_cases.LinkExerciseWithWorkoutDay
import com.esteban.ruano.workout_domain.use_cases.RemoveSet
import com.esteban.ruano.workout_domain.use_cases.SaveExercise
import com.esteban.ruano.workout_domain.use_cases.SaveWorkoutDay
import com.esteban.ruano.workout_domain.use_cases.UpdateWorkoutDay
import com.esteban.ruano.workout_domain.use_cases.WorkoutUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object WorkoutDomainModule {
    @ViewModelScoped
    @Provides
    fun provideTaskUseCases(
        repository: WorkoutRepository
    ): WorkoutUseCases {
        return WorkoutUseCases(
            getWorkoutDays = GetWorkoutDays(repository),
            getWorkoutDaysWithExercises = GetWorkoutDaysWithExercises(repository),
            getWorkoutDayById = GetWorkoutDayById(repository),
            getExercisesByWorkoutDay = GetExercisesByWorkoutDay(repository),
            getWorkoutDayByNumber = GetWorkoutDayByNumber(repository),
            linkExerciseToWorkoutDay = LinkExerciseWithWorkoutDay(repository),
            saveExercise = SaveExercise(repository),
            saveWorkoutDay = SaveWorkoutDay(repository),
            getExercises = GetExercises(repository),
            updateWorkoutDay = UpdateWorkoutDay(repository),
            getWorkoutDashboard = GetWorkoutDashboard(repository),
            getExerciseById = GetExerciseById(repository),
            addSet = AddSet(repository),
            getWorkoutDayStatus = GetWorkoutDayStatus(repository),
            removeSet = RemoveSet(repository)
        )
    }
}