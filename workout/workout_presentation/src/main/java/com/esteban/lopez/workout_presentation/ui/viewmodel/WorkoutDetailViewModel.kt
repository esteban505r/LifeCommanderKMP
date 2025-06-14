package com.esteban.ruano.workout_presentation.ui.viewmodel

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.utils.DateUtils.formatElapsedTime
import com.esteban.ruano.core.utils.DateUtils.formatTime
import com.esteban.ruano.core.utils.UiEvent
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.use_cases.WorkoutUseCases
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.models.ExerciseInProgress
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailEffect
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    private val workoutUseCases: WorkoutUseCases
) : BaseViewModel<WorkoutIntent, WorkoutDayDetailState, WorkoutDayDetailEffect>() {

    var elapsedTime: Long = 0

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            elapsedTime++
            emitState {
                copy(
                    time = formatElapsedTime(elapsedTime)
                )
            }
            if ((elapsedTime % 2) == 0L) {
                val currentExercise =
                    currentState.exercisesInProgress.firstOrNull { it.done.not() }
                if (currentExercise != null) {
                    currentExercise.exercise.id?.let {
                        doRep(id = it)
                    }
                }
            }
            handler.postDelayed(this, 1000)
        }
    }


    private fun updateWorkoutDayExercises(id: String, exercises: List<Exercise>) {
        viewModelScope.launch {
            val result = workoutUseCases.updateWorkoutDay(
                id, currentState.workoutDay?.copy(
                    exercises = exercises
                )?: return@launch
            )
            result.fold(
                onSuccess = {
                    sendEffect {
                        WorkoutDayDetailEffect.NavigateUp
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

    private fun undoRep(id: String) {
        viewModelScope.launch {
            val updatedExercises = currentState.exercisesInProgress.map { exerciseInProgress ->
                if (exerciseInProgress.exercise.id == id) {
                    val (repsDone, setsDone) = decreaseRepsAndSetsDone(exerciseInProgress)
                    exerciseInProgress.copy(repsDone = repsDone, setsDone = setsDone)
                } else {
                    exerciseInProgress
                }
            }
            emitState {
                copy(exercisesInProgress = updatedExercises)
            }
        }
    }

private fun doRep(id: String) {
    viewModelScope.launch {
        val updatedExercises = currentState.exercisesInProgress.map { exerciseInProgress ->
            if (exerciseInProgress.exercise.id == id) {
                val (repsDone, setsDone) = increaseRepsAndSetsDone(exerciseInProgress)
                val done = setsDone >= exerciseInProgress.exercise.baseSets
                exerciseInProgress.copy(
                    setsDone = setsDone,
                    repsDone = repsDone,
                    done = done
                )
            } else {
                exerciseInProgress
            }
        }

        val toUpdate = updatedExercises.first { it.exercise.id == id }
        if (toUpdate.setsDone >= toUpdate.exercise.baseSets) {
            sendEffect {
                WorkoutDayDetailEffect.AnimateToNextExercise
            }
        }

        emitState{ copy(exercisesInProgress = updatedExercises) }

    }
}

private fun increaseRepsAndSetsDone(exerciseInProgress: ExerciseInProgress): Pair<Int, Int> {
    return if (exerciseInProgress.repsDone < exerciseInProgress.exercise.baseReps) {
        Pair(exerciseInProgress.repsDone + 1, exerciseInProgress.setsDone)
    } else {
        if (exerciseInProgress.setsDone < exerciseInProgress.exercise.baseSets) {
            val repsDone =
                if (exerciseInProgress.setsDone + 1 >= exerciseInProgress.exercise.baseSets) exerciseInProgress.repsDone else 0
            Pair(repsDone, exerciseInProgress.setsDone + 1)
        } else {
            Pair(exerciseInProgress.repsDone, exerciseInProgress.setsDone)
        }
    }
}

private fun decreaseRepsAndSetsDone(exerciseInProgress: ExerciseInProgress): Pair<Int, Int> {
    return if (exerciseInProgress.repsDone > 0) {
        Pair(exerciseInProgress.repsDone - 1, exerciseInProgress.setsDone)
    } else {
        if (exerciseInProgress.setsDone > 0) {
            Pair(exerciseInProgress.exercise.baseReps, exerciseInProgress.setsDone - 1)
        } else {
            Pair(0, 0)
        }
    }
}


private fun fetchWorkoutDayById(
    id: Int
) {
    viewModelScope.launch {
        val result = workoutUseCases.getWorkoutDayById(id)
        result.fold(
            onSuccess = {
                emitState {
                    copy(
                        workoutDay = it,
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


override fun createInitialState(): WorkoutDayDetailState {
    return WorkoutDayDetailState()
}

override fun handleIntent(intent: WorkoutIntent) {
    intent.let {
        when (it) {
            is WorkoutIntent.FetchWorkoutDayById -> fetchWorkoutDayById(
                id = it.id.toIntOrNull() ?: -1
            )

            is WorkoutIntent.CompleteExercise -> {
                // completeExercise(id = it.id)
            }

            is WorkoutIntent.DoRep -> {
                doRep(id = it.id)
            }

            is WorkoutIntent.UndoRep -> {
                undoRep(id = it.id)
            }

            is WorkoutIntent.StartTimer -> {
                elapsedTime = 0
                handler.post(runnable)
            }

            is WorkoutIntent.UpdateWorkoutDayExercises -> {
                updateWorkoutDayExercises(id = it.id, exercises = it.exercises)
            }

            else -> {}
        }
    }
}

}