package com.esteban.ruano.habits_presentation.ui.screens.viewmodel.state

import com.esteban.ruano.core.utils.AppConstants.EMPTY_STRING
import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.habits_domain.model.Habit

data class HabitState(
    val habits: List<Habit> = emptyList(),
    val dateRangeSelectedIndex : Int = 0,
    val offlineModeEnabled : Boolean = false,
    val date: String,
    val filter : String = EMPTY_STRING,
    val isLoading : Boolean = false,
    val isRefreshing : Boolean = false,
    val isError : Boolean = false,
    val errorMessage : String = EMPTY_STRING,
): ViewState