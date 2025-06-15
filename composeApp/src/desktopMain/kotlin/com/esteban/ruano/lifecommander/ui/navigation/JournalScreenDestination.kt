package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.*
import org.koin.compose.viewmodel.koinViewModel
import ui.viewmodels.DailyJournalViewModel
import com.esteban.ruano.lifecommander.ui.screens.JournalScreen

@Composable
fun JournalScreenDestination() {
    val viewModel: DailyJournalViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    var answers by remember { mutableStateOf(mutableMapOf<String, String>()) }

    JournalScreen(
        state = state,
        answers = answers,
        onAnswerChange = { questionId, ans ->
            answers = answers.toMutableMap().apply { put(questionId, ans) }
            viewModel.addAnswer(questionId, ans)
        },
        onComplete = { viewModel.completeDailyJournal() },
        onLoadQuestions = { viewModel.loadQuestions() }
    )
} 