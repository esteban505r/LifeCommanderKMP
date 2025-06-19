package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import services.dailyjournal.models.QuestionAnswerDTO
import services.dailyjournal.models.QuestionDTO
import ui.viewmodels.DailyJournalState
import ui.composables.NewEditQuestionDialog

@Composable
fun JournalScreen(
    state: DailyJournalState,
    answers: List<QuestionAnswerDTO>,
    onAnswerChange: (String, String) -> Unit,
    onComplete: () -> Unit,
    onLoadQuestions: () -> Unit,
    onAddQuestion: (String) -> Unit,
    onEditQuestion: (String, String) -> Unit,
    onDeleteQuestion: (String) -> Unit,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var showEditQuestionDialog by remember { mutableStateOf<QuestionDTO?>(null) }
    var showDeleteQuestionDialog by remember { mutableStateOf<QuestionDTO?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Journal",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold
            )
            if (!isCompleted) {
                Button(
                    onClick = { showAddQuestionDialog = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Question")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Question")
                }
            }
        }

        if (isCompleted) {
            // Show completed journal with answers
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.questions.size) { idx ->
                    val question = state.questions[idx]
                    val answer = answers.firstOrNull { it.questionId == question.id }?.answer ?: ""
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
                            Text(
                                text = answer,
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                }
            }
        } else {
            // Show questions to answer
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.questions.size) { idx ->
                    val question = state.questions[idx]
                    JournalQuestionCard(
                        question = question,
                        answer = answers.firstOrNull { it.questionId == question.id }?.answer ?: "",
                        onAnswerChange = { newAnswer ->
                            onAnswerChange(question.id, newAnswer)
                        },
                        onEdit = { showEditQuestionDialog = question },
                        onDelete = { showDeleteQuestionDialog = question }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { showCompleteDialog = true },
                enabled = state.questions.all { question -> answers.firstOrNull{it.questionId == question.id}?.answer?.isNotBlank() == true },
            ) {
                Text("Complete Journal")
            }
        }
    }

    // Complete Journal Dialog
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

    // Add Question Dialog
    if (showAddQuestionDialog) {
        NewEditQuestionDialog(
            show = showAddQuestionDialog,
            title = "Add New Question",
            initialQuestion = "",
            existingQuestions = state.questions.map { it.question },
            onDismiss = { showAddQuestionDialog = false },
            onConfirm = { question ->
                onAddQuestion(question)
                showAddQuestionDialog = false
            }
        )
    }

    // Edit Question Dialog
    showEditQuestionDialog?.let { question ->
        NewEditQuestionDialog(
            show = true,
            title = "Edit Question",
            initialQuestion = question.question,
            existingQuestions = state.questions.filter { it.id != question.id }.map { it.question },
            onDismiss = { showEditQuestionDialog = null },
            onConfirm = { newQuestion ->
                onEditQuestion(question.id, newQuestion)
                showEditQuestionDialog = null
            }
        )
    }

    // Delete Question Dialog
    showDeleteQuestionDialog?.let { question ->
        AlertDialog(
            onDismissRequest = { showDeleteQuestionDialog = null },
            title = { Text("Delete Question") },
            text = { Text("Are you sure you want to delete this question?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteQuestion(question.id)
                        showDeleteQuestionDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteQuestionDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun JournalQuestionCard(
    question: QuestionDTO,
    answer: String,
    onAnswerChange: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var tempAnswer by remember { mutableStateOf(answer) }
    var showUpdateButton by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Question")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Question")
                    }
                }
            }
            OutlinedTextField(
                value = tempAnswer,
                onValueChange = { 
                    tempAnswer = it
                    showUpdateButton = it != answer
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Your answer...") },
                maxLines = 3
            )
            if (showUpdateButton) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { 
                            tempAnswer = answer
                            showUpdateButton = false
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { 
                            onAnswerChange(tempAnswer)
                            showUpdateButton = false
                        },
                        enabled = tempAnswer.isNotBlank()
                    ) {
                        Text(if (answer.isBlank()) "Add Answer" else "Update Answer")
                    }
                }
            }
        }
    }
} 