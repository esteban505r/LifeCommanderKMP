package com.esteban.ruano.journal_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core.utils.UiText
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.journal_presentation.intent.JournalEffect
import com.esteban.ruano.journal_presentation.intent.JournalIntent
import com.esteban.ruano.journal_presentation.ui.JournalScreen
import com.esteban.ruano.journal_presentation.ui.viewmodel.JournalViewModel

@Composable
fun JournalDestination(
    viewModel: JournalViewModel = hiltViewModel(),
    navController: NavController,
) {
    val state = viewModel.viewState.collectAsState().value
    val sendMainIntent = LocalMainIntent.current

    LaunchedEffect(Unit) {
        viewModel.performAction(JournalIntent.InitializeJournal)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is JournalEffect.ShowError -> {
                    sendMainIntent(
                        MainIntent.ShowSnackBar(
                            message = effect.message,
                            type = SnackbarType.ERROR
                        )
                    )
                }
                is JournalEffect.NavigateToHistory -> {
                    navController.navigate(com.esteban.ruano.core.routes.Routes.JOURNAL_HISTORY)
                }
            }
        }
    }

    JournalScreen(
        state = state,
        onAnswerChange = { questionId, answer ->
            viewModel.performAction(JournalIntent.AddAnswer(questionId, answer))
        },
        onComplete = {
            viewModel.performAction(JournalIntent.CompleteDailyJournal)
        },
        onLoadQuestions = {
            viewModel.performAction(JournalIntent.LoadQuestions)
        },
        onAddQuestion = { question, type ->
            viewModel.performAction(JournalIntent.AddQuestion(question, type))
        },
        onEditQuestion = { id, question, type ->
            viewModel.performAction(JournalIntent.UpdateQuestion(id, question, type))
        },
        onDeleteQuestion = { id ->
            viewModel.performAction(JournalIntent.DeleteQuestion(id))
        },
        onViewHistory = {
            navController.navigate(com.esteban.ruano.core.routes.Routes.JOURNAL_HISTORY)
        }
    )
}

