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
import com.esteban.ruano.habits_presentation.ui.intent.HabitEffect
import com.esteban.ruano.habits_presentation.ui.intent.HabitIntent
import com.esteban.ruano.habits_presentation.ui.screens.NewHabitScreen
import com.esteban.ruano.habits_presentation.ui.screens.viewmodel.HabitDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun NewHabitDestination(
    viewModel: HabitDetailViewModel = hiltViewModel(),
    habitToEditId: String? = null,
    onNavigateUp: () -> Unit,
) {

    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    /**
     * Handles navigation based on [HabitEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: HabitEffect) {
        when (effect) {
            HabitEffect.NavigateUp -> onNavigateUp()
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        coroutineScope.launch { viewModel.effect.collect { handleNavigation(it) } }

       if(habitToEditId!=null){
           coroutineScope.launch {
               viewModel.performAction(HabitIntent.FetchHabit(habitToEditId))
           }
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
            NewHabitScreen(
                onClose = onNavigateUp,
                state = state,
                userIntent = {
                    viewModel.performAction(it)
                }
            )
        }
    }


}