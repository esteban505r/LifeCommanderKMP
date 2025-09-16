package com.esteban.ruano.tasks_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.tasks_presentation.intent.TaskEffect
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.TasksScreen
import com.esteban.ruano.tasks_presentation.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@Composable
fun TasksDestination(
    viewModel: TaskViewModel = hiltViewModel(),
    navController: NavController,
) {

    val state = viewModel.viewState.collectAsState().value
    val sendMainIntent = LocalMainIntent.current
    val coroutineScope = rememberCoroutineScope()


    /**
     * Handles navigation based on [TaskEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: TaskEffect) {
        when (effect) {
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        coroutineScope.launch { viewModel.effect.collect { handleNavigation(it) } }

        coroutineScope.launch {
            viewModel.performAction(
                TaskIntent.FetchTasks()
            )
        }
        /*coroutineScope.launch {
            viewModel.performAction(
                TaskIntent.TrySync {
                    coroutineScope.launch {
                        sendMainIntent(
                            MainIntent.Sync
                        )
                    }
                }
            )
        }*/
        coroutineScope.launch {
            viewModel.performAction(
                TaskIntent.FetchIsOfflineModeEnabled(
                    onOnlineMode = {},
                    onOfflineMode = {}
                )
            )
        }
    }

    when {
        state.isError -> {
            Error(
                message = stringResource(R.string.error_unknown),
            )
        }

        else -> {
            TasksScreen(
                onNavigateUp = { navController.popBackStack() },
                onTaskClick = {
                    navController.navigate("${Routes.TASK_DETAIL}/${it.id}")
                },
                onNewTaskClick = {
                    navController.navigate(Routes.NEW_EDIT_TASK)
                },
                userIntent = {
                    viewModel.performAction(it)
                },
                state = state
            )
        }
    }


}