package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.workout.day.UpdateWorkoutDay
import com.esteban.ruano.lifecommander.services.workout.WorkoutService
import com.esteban.ruano.lifecommander.ui.state.WorkoutState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val service: WorkoutService
) : ViewModel() {
    private val _state = MutableStateFlow(WorkoutState())
    val state: StateFlow<WorkoutState> = _state.asStateFlow()

    fun getExercisesByDay(day: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val exercises = service.getExercisesByDay(day)
                _state.value = _state.value.copy(
                    exercises = exercises,
                    isLoading = false,
                    isError = false,
                    errorMessage = "",
                    daySelected = day
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            try {
                service.addExercise(exercise)
                getExercisesByDay(_state.value.daySelected)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            try {
                service.updateExercise(exercise)
                getExercisesByDay(_state.value.daySelected)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun deleteExercise(id: String) {
        viewModelScope.launch {
            try {
                service.deleteExercise(id)
                getExercisesByDay(_state.value.daySelected)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }




} 