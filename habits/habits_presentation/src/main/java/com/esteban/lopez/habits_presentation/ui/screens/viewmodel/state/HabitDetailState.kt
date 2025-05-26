package com.esteban.ruano.habits_presentation.ui.screens.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.lifecommander.models.Habit

data class HabitDetailState (
    val habit: Habit? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
):ViewState