package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.services.workout.WorkoutService
import com.esteban.ruano.lifecommander.ui.state.WorkoutState
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class WorkoutViewModel(
    private val service: WorkoutService
) : ViewModel() {
    private val _state = MutableStateFlow(WorkoutState())
    val state: StateFlow<WorkoutState> = _state.asStateFlow()

    fun getExercisesByDay(day: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val now = getCurrentDateTime(TimeZone.currentSystemDefault())
                val dayOfWeek = now.dayOfWeek.ordinal + 1
                val weekStart = now.date.minus(DatePeriod(days = dayOfWeek - 1))
                val dateToSend = weekStart.plus(DatePeriod(days = day - 1 ))
                val workoutDays = service.getExercisesByDay(day, dateToSend.atTime(12,0).formatDefault())
                val exercises = workoutDays.flatMap { it.exercises ?: emptyList() }
                val exerciseDayMap = fetchExerciseDayMap()
                
                // Get completed exercises for the current workout day
                val completedExercises = if (workoutDays.isNotEmpty()) {
                    val workoutDayId = workoutDays.first().id
                    service.getCompletedExercisesForDay(workoutDayId, now.formatDefault())
                } else {
                    emptyList()
                }
                
                _state.value = _state.value.copy(
                    exercises = exercises,
                    workoutDays = workoutDays,
                    completedExercises = completedExercises.toSet(),
                    isLoading = false,
                    isError = false,
                    errorMessage = "",
                    daySelected = day,
                    exerciseDayMap = exerciseDayMap,
                    isCompleted = workoutDays.firstOrNull()?.isCompleted == true
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

    // Workout Tracking Methods
    fun completeWorkout(dayId: Int) {
        viewModelScope.launch {
            try {
                val now = getCurrentDateTime(TimeZone.currentSystemDefault())
                val dayOfWeek = now.dayOfWeek.ordinal+1
                val weekStart = now.date.minus(DatePeriod(days = dayOfWeek - 1))
                val dateToSend = weekStart.plus(DatePeriod(days = dayId - 1 ))
                val success = service.completeWorkout(dayId, dateToSend.atTime(4,0).formatDefault())
                if (success) {
                    // Refresh the current day's exercises to show updated state
                    getExercisesByDay(_state.value.daySelected)
                } else {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "Failed to complete workout"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun unCompleteWorkout(trackId: String) {
        viewModelScope.launch {
            try {
                val success = service.unCompleteWorkout(trackId)
                if (success) {
                    // Refresh the current day's exercises to show updated state
                    getExercisesByDay(_state.value.daySelected)
                } else {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "Failed to uncomplete workout"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun getWorkoutsCompletedPerDayThisWeek() {
        viewModelScope.launch {
            try {
                val weeklyData = service.getWorkoutsCompletedPerDayThisWeek()
                _state.value = _state.value.copy(
                    weeklyWorkoutsCompleted = weeklyData,
                    isError = false,
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun getWorkoutTracksByDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            try {
                val tracks = service.getWorkoutTracksByDateRange(startDate, endDate)
                _state.value = _state.value.copy(
                    workoutTracks = tracks,
                    isError = false,
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun deleteWorkoutTrack(trackId: String) {
        viewModelScope.launch {
            try {
                val success = service.deleteWorkoutTrack(trackId)
                if (success) {
                    // Refresh the current day's exercises to show updated state
                    getExercisesByDay(_state.value.daySelected)
                } else {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "Failed to delete workout track"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun getAllExercises() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val exercises = service.getExercises()
                val exerciseDayMap = fetchExerciseDayMap()
                _state.value = _state.value.copy(
                    exercises = exercises,
                    isLoading = false,
                    isError = false,
                    errorMessage = "",
                    exerciseDayMap = exerciseDayMap
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

    fun bindExerciseToDay(exerciseId: String, workoutDayId: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val success = service.bindExerciseToDay(exerciseId, workoutDayId)
                onResult(success)
                // Optionally refresh exercises for the day or all
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun unbindExerciseFromDay(exerciseId: String, workoutDayId: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val success = service.unbindExerciseFromDay(exerciseId, workoutDayId)
                onResult(success)
                // Optionally refresh exercises for the day or all
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    private suspend fun fetchExerciseDayMap(): Map<String, Set<Int>> {
        return try {
            val days = service.getAllWorkoutDays()
            val map = mutableMapOf<String, MutableSet<Int>>()
            days.forEach { day ->
                day.day?.let { d ->
                    day.exercises?.forEach { ex ->
                        map.getOrPut(ex.id?:"") { mutableSetOf() }.add(d)
                    }
                }
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun changeAllExercisesMode(
        value: Boolean = !_state.value.allExerciseMode
    ) {
        _state.value = _state.value.copy(
            allExerciseMode = value
        )
    }

    fun addExerciseSet(exerciseId: String, workoutDayId: String, reps: Int) {
        viewModelScope.launch {
            try {
                val now = getCurrentDateTime(TimeZone.currentSystemDefault())
                val success = service.completeExerciseSet(
                    exerciseId = exerciseId,
                    workoutDayId = workoutDayId,
                    reps = reps,
                    doneDateTime = now.formatDefault()
                )
                if (success) {
                    getExercisesByDay(_state.value.daySelected)
                } else {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "Failed to complete exercise set"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun unCompleteExerciseSet(setTrackId: String) {
        viewModelScope.launch {
            try {
                val success = service.unCompleteExerciseSet(setTrackId)
                if (success) {
                    getExercisesByDay(_state.value.daySelected)
                } else {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "Failed to uncomplete exercise set"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }


    // Exercise Tracking Methods
    fun completeExercise(exerciseId: String, day:Int, workoutDayId: String) {
        viewModelScope.launch {
            try {
                val now = getCurrentDateTime(TimeZone.currentSystemDefault())
                val dayOfWeek = now.dayOfWeek.ordinal + 1
                val weekStart = now.date.minus(DatePeriod(days = dayOfWeek - 1))
                val dateToSend = weekStart.plus(DatePeriod(days = day - 1 ))
                val success = service.completeExercise(exerciseId, workoutDayId, dateToSend.atTime(4,0).formatDefault())
                if (success) {
                    // Refresh the current day's exercises to show updated state
                    getExercisesByDay(_state.value.daySelected)
                } else {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "Failed to complete exercise"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun unCompleteExercise(trackId: String) {
        viewModelScope.launch {
            try {
                val success = service.unCompleteExercise(trackId)
                if (success) {
                    // Refresh the current day's exercises to show updated state
                    getExercisesByDay(_state.value.daySelected)
                } else {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "Failed to uncomplete exercise"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun getExerciseTracksByDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            try {
                val tracks = service.getExerciseTracksByDateRange(startDate, endDate)
                _state.value = _state.value.copy(
                    exerciseTracks = tracks,
                    isError = false,
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }


} 