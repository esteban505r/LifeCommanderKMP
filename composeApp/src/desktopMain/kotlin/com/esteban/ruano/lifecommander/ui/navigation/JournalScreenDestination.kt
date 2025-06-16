package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.*
import org.koin.compose.viewmodel.koinViewModel
import ui.viewmodels.DailyJournalViewModel
import com.esteban.ruano.lifecommander.ui.screens.JournalScreen

@Composable
fun JournalScreenDestination() {
    val viewModel: DailyJournalViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadQuestions()
        viewModel.checkIfCompleted()
    }

    JournalScreen(
        state = state,
        answers = state.questionAnswers,
        onAnswerChange = { questionId, ans ->
            viewModel.addAnswer(questionId, ans)
        },
        onComplete = { viewModel.completeDailyJournal() },
        onLoadQuestions = { viewModel.loadQuestions() },
        onAddQuestion = { question ->
            viewModel.addQuestion(question)
        },
        onDeleteQuestion = { questionId ->
            viewModel.deleteQuestion(questionId)
        },
        onEditQuestion = { id, question ->
            viewModel.updateQuestion(id, question)
        },
        isCompleted = state.isCompleted
    )
} 