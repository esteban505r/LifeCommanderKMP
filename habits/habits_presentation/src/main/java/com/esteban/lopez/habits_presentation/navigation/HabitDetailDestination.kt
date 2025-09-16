package com.esteban.ruano.habits_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.lifecommander.ui.components.Loading
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.habits_presentation.ui.intent.HabitEffect
import com.esteban.ruano.habits_presentation.ui.intent.HabitIntent
import com.esteban.ruano.habits_presentation.ui.screens.HabitDetailScreen
import com.esteban.ruano.habits_presentation.ui.screens.viewmodel.HabitDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun HabitDetailDestination(
    viewModel: HabitDetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    habitId: String,
    onEditClick: (String) -> Unit
) {

    val state = viewModel.viewState.collectAsState().value
    val sendMainIntent = LocalMainIntent.current
    val coroutineScope = rememberCoroutineScope()


    /**
     * Handles navigation based on [HabitEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: HabitEffect) {
        when (effect) {
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        coroutineScope.launch { viewModel.effect.collect { handleNavigation(it) } }

        coroutineScope.launch {
            viewModel.performAction(HabitIntent.FetchHabit(habitId))
        }
    }

    when {
        state.errorMessage!=null -> {
            Error(
                message = stringResource(R.string.error_unknown),
            )
        }

        state.isLoading -> {
            Loading()
        }


        else -> {
            HabitDetailScreen(
                onNavigateUp = onNavigateUp,
                userIntent = {
                    viewModel.performAction(it)
                },
                state = state,
                habitId = habitId,
                onEditClick = {
                    onEditClick(habitId)
                }
            )
        }
    }


}