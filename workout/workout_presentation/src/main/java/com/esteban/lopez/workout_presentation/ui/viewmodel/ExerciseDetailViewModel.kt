package com.esteban.ruano.workout_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.workout_domain.use_cases.WorkoutUseCases
import com.esteban.ruano.workout_presentation.intent.ExerciseDetailEffect
import com.esteban.ruano.workout_presentation.intent.ExerciseDetailIntent
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.ExerciseDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val workoutUseCases: WorkoutUseCases
) : BaseViewModel<ExerciseDetailIntent, ExerciseDetailState, ExerciseDetailEffect>() {


    override fun handleIntent(intent: ExerciseDetailIntent) {
        intent.let {
            when (it) {
                is ExerciseDetailIntent.FetchExercise -> fetchExercise(
                    it.exerciseId
                )

                ExerciseDetailIntent.NavigateUp -> {
                    sendEffect {
                        ExerciseDetailEffect.NavigateUp
                    }
                }
            }
        }
    }


    private fun fetchExercise(
        exerciseId: String
    ) {
        viewModelScope.launch {
            val result = workoutUseCases.getExerciseById(
                exerciseId
            )
            result.fold(
                onSuccess = {
                    emitState {
                        copy(
                            exercise = it
                        )
                    }
                },
                onFailure = {
                    emitState {
                        copy(
                            errorMessage = it.message
                        )
                    }
                }
            )
        }
    }


    override fun createInitialState(): ExerciseDetailState {
        return ExerciseDetailState()
    }



}