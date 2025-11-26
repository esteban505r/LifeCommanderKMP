package com.esteban.ruano.timers_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.timers_presentation.ui.intent.TimerEffect
import com.esteban.ruano.timers_presentation.ui.intent.TimerIntent
import com.esteban.ruano.timers_presentation.ui.screens.TimersScreen
import com.esteban.ruano.timers_presentation.ui.screens.viewmodel.TimerViewModel
import kotlinx.coroutines.launch

@Composable
fun TimersDestination(
    viewModel: TimerViewModel = hiltViewModel(),
    navController: NavController
) {
    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    fun handleNavigation(effect: TimerEffect) {
        when (effect) {
            is TimerEffect.NavigateUp -> navController.popBackStack()
            is TimerEffect.NavigateToTimerListDetail -> {
                navController.navigate("${Routes.TIMER_LIST_DETAIL}/${effect.listId}")
            }
            is TimerEffect.ShowSnackBar -> {
                // Handle snackbar via global snackbar controller
            }
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            viewModel.effect.collect { handleNavigation(it) }
        }
        viewModel.performAction(TimerIntent.FetchTimerLists)
        // WebSocket is now managed by TimerServiceManager at app level
    }

    when {
        state.isError -> {
            Error(
                message = stringResource(R.string.error_unknown)
            )
        }
        else -> {
            TimersScreen(
                state = state,
                userIntent = { viewModel.performAction(it) },
                onNavigateUp = { navController.popBackStack() },
                onTimerListClick = { listId ->
                    navController.navigate("${Routes.TIMER_LIST_DETAIL}/$listId")
                }
            )
        }
    }
}

