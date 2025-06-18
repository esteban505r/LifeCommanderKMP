package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import org.koin.compose.viewmodel.koinViewModel
import ui.viewmodels.DailyJournalViewModel
import com.esteban.ruano.lifecommander.ui.screens.JournalScreen

@Composable
fun JournalScreenDestination() {
    val viewModel: DailyJournalViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    var showAddQuestionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initializeJournal()
    }

    when {
        state.isLoading -> {
            LoadingScreen(
                message = "Loading journal...",
                modifier = Modifier
            )
        }
        state.error != null -> {
            ErrorScreen(
                message = state.error ?: "Failed to load journal",
                onRetry = { 
                    viewModel.initializeJournal()
                },
                modifier = Modifier
            )
        }
        state.isCompleted -> {
            // Show completed journal with answers
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
                isCompleted = true
            )
        }
        state.showQuestions -> {
            // Show questions to answer
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
                isCompleted = false
            )
        }
        else -> {
            // Empty state - no questions and not completed
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = "Empty Journal",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "No Journal Questions",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.87f)
                    )
                    Text(
                        text = "Add your first journal question to get started with daily reflection.",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddQuestionDialog = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Question")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Your First Question")
                    }
                }
            }
        }
    }

    // Add Question Dialog for empty state
    if (showAddQuestionDialog) {
        ui.composables.NewEditQuestionDialog(
            show = showAddQuestionDialog,
            title = "Add Your First Question",
            initialQuestion = "",
            existingQuestions = emptyList(),
            onDismiss = { showAddQuestionDialog = false },
            onConfirm = { question ->
                viewModel.addQuestion(question)
                showAddQuestionDialog = false
            }
        )
    }
} 