package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import services.dailyjournal.models.QuestionDTO
import ui.viewmodels.DailyJournalState

@Composable
fun JournalScreen(
    state: DailyJournalState,
    answers: Map<String, String>,
    onAnswerChange: (String, String) -> Unit,
    onComplete: () -> Unit,
    onLoadQuestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCompleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onLoadQuestions()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Daily Journal",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = MaterialTheme.colors.error)
                }
            }
            state.isCompleted -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Journal completed for today!", color = MaterialTheme.colors.primary)
                }
            }
            state.showQuestions -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.questions.size) { idx ->
                        val question = state.questions[idx]
                        JournalQuestionCard(
                            question = question,
                            answer = answers[question.id] ?: "",
                            onAnswerChange = { ans ->
                                onAnswerChange(question.id, ans)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showCompleteDialog = true },
                    enabled = state.questions.all { answers[it.id]?.isNotBlank() == true }
                ) {
                    Text("Complete Journal")
                }
            }
        }
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Submit Journal") },
            text = { Text("Are you sure you want to submit your journal for today?") },
            confirmButton = {
                Button(onClick = {
                    onComplete()
                    showCompleteDialog = false
                }) { Text("Submit") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCompleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun JournalQuestionCard(
    question: QuestionDTO,
    answer: String,
    onAnswerChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Your answer...") },
                maxLines = 3
            )
        }
    }
} 