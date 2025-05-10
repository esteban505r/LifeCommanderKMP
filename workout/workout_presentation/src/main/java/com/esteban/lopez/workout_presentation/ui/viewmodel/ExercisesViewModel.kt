package com.esteban.ruano.workout_presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.utils.UiEvent
import com.esteban.ruano.core.utils.UiText
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.WorkoutDay
import com.esteban.ruano.workout_domain.use_cases.WorkoutUseCases
import com.esteban.ruano.workout_presentation.intent.ExercisesEffect
import com.esteban.ruano.workout_presentation.intent.ExercisesIntent
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.ExercisesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val workoutUseCases: WorkoutUseCases
) : BaseViewModel<ExercisesIntent,ExercisesState, ExercisesEffect>() {


    override fun handleIntent(intent: ExercisesIntent) {
        intent.let {
            when (it) {
                is ExercisesIntent.FetchExercisesByWorkoutDay -> fetchExercisesByWorkoutDay(
                    it.workoutDayId
                )

                is ExercisesIntent.SaveExercise -> saveExercise(
                    it.exercise
                )

                ExercisesIntent.FetchExercises -> fetchExercises()
                is ExercisesIntent.NavigateUp -> {
                    sendEffect {
                        ExercisesEffect.NavigateUp
                    }
                }
            }
        }
    }


    private fun fetchExercises() {
        viewModelScope.launch {
            val result = workoutUseCases.getExercises()
            result.fold(
                onSuccess = {
                    emitState {
                        copy(
                            exercises = it
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

    private fun saveExercise(
        exercise: Exercise
    ) {
        viewModelScope.launch {
            //Saving the exercise
            val result = workoutUseCases.saveExercise(
                exercise
            )
            result.onFailure {
                 sendEffect {
                    ExercisesEffect.ShowSnackbarErrorMessage()
                }
                return@launch
            }

            emitState {
                copy(
                    showNewExerciseDialog = false
                )
            }

            sendEffect {
                ExercisesEffect.NavigateUp
            }


        }
    }


    private fun fetchExercisesByWorkoutDay(
        workoutDayId: Int
    ) {
        viewModelScope.launch {
            emitState {
                copy(
                    loading = true
                )
            }
            val result = workoutUseCases.getExercisesByWorkoutDay(
                workoutDayId
            )
            result.fold(
                onSuccess = {
                    emitState {
                        copy(
                            exercises = it,
                            loading = false
                        )
                    }
                },
                onFailure = {
                    emitState {
                        copy(
                            errorMessage = it.message,
                            loading = false
                        )
                    }
                }
            )
        }
    }

    override fun createInitialState(): ExercisesState {
        return ExercisesState()
    }



}