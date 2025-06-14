package com.esteban.ruano.lifecommander.ui.state

import com.esteban.ruano.lifecommander.models.Exercise


data class WorkoutState(
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val daySelected: Int = 0
) 