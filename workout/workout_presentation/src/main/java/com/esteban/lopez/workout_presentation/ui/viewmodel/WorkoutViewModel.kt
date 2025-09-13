package com.esteban.ruano.workout_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.workout_domain.use_cases.WorkoutUseCases
import com.esteban.ruano.workout_presentation.intent.WorkoutEffect
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutUseCases: WorkoutUseCases
) : BaseViewModel<WorkoutIntent, WorkoutState, WorkoutEffect>() {

    private fun fetchDashboard(
    ) {
        viewModelScope.launch {
            val result = workoutUseCases.getWorkoutDashboard()
            result.fold(
                {
                    emitState {
                        currentState.copy(
                            workouts = it.workouts,
                            totalExercises = it.totalExercises,
                        )
                    }
                },
                {

                }
            )
        }
    }

    override fun createInitialState(): WorkoutState {
        return WorkoutState()
    }

    override fun handleIntent(intent: WorkoutIntent) {
        intent.let {
            when (it) {
                is WorkoutIntent.FetchDashboard -> fetchDashboard()
                else -> {}
            }
        }
    }


}