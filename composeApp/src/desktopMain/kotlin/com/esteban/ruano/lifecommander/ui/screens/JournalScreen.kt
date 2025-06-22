package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.lifecommander.ui.components.JournalQuestionCard
import services.dailyjournal.models.MoodType
import services.dailyjournal.models.QuestionAnswerDTO
import services.dailyjournal.models.QuestionDTO
import services.dailyjournal.models.QuestionType
import ui.composables.NewEditQuestionDialog
import ui.viewmodels.DailyJournalState

@Composable
fun JournalScreen(
    state: DailyJournalState,
    answers: List<QuestionAnswerDTO>,
    onAnswerChange: (String, String) -> Unit,
    onComplete: () -> Unit,
    onLoadQuestions: () -> Unit,
    onAddQuestion: (String, QuestionType) -> Unit,
    onEditQuestion: (String, String, QuestionType) -> Unit,
    onDeleteQuestion: (String) -> Unit,
    onViewHistory: () -> Unit,
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
            .background(MaterialTheme.colors.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = "Journal",
                                    tint = MaterialTheme.colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
            Text(
                text = "Daily Journal",
                style = MaterialTheme.typography.h4,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onSurface
                            )
                            Text(
                                text = if (isCompleted) "Completed for today" else "Reflect on your day",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // History button
                        OutlinedButton(
                            onClick = onViewHistory,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colors.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = "View History",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("History")
                        }
                        
            if (!isCompleted) {
                Button(
                    onClick = { showAddQuestionDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                ),
                    modifier = Modifier.height(40.dp)
                ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Question",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Question")
                }
            }
        }
                }
                
                // Progress indicator
                if (!isCompleted && state.questions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val answeredCount = state.questions.count { question ->
                        answers.firstOrNull { it.questionId == question.id }?.answer?.isNotBlank() == true
                    }
                    val progress = answeredCount.toFloat() / state.questions.size
                    
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                text = "Progress",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                text = "$answeredCount/${state.questions.size}",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }

        // Content
        if (isCompleted) {
            // Show completed journal with answers
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.questionAnswers) { question ->
                    CompletedJournalCard(
                        question = question,
                        answer = answers.firstOrNull { it.questionId == question.questionId }?.answer ?: ""
                    )
                }
            }
        } else {
            // Show questions to answer
            if (state.questions.isEmpty()) {
                EmptyState(
                    onAddQuestion = { showAddQuestionDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.questions) { question ->
                        JournalQuestionCard(
                            question = question,
                            answer = answers.firstOrNull { it.questionId == question.id }?.answer ?: "",
                            onAnswerChanged = { newAnswer ->
                                onAnswerChange(question.id, newAnswer)
                            },
                            onEdit = { showEditQuestionDialog = question },
                            onDelete = { showDeleteQuestionDialog = question }
                        )
                    }
                }
                
                // Complete button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val allAnswered = state.questions.all { question ->
                            answers.firstOrNull { it.questionId == question.id }?.answer?.isNotBlank() == true
                        }
                        
                        Button(
                            onClick = { showCompleteDialog = true },
                            enabled = allAnswered,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (allAnswered) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Complete",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (allAnswered) "Complete Journal" else "Answer all questions to complete",
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Complete Journal Dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Complete Journal",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to submit your journal for today? This action cannot be undone.",
                    style = MaterialTheme.typography.body1
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                    onComplete()
                    showCompleteDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Submit")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCompleteDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
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
            onConfirm = { question, type ->
                onAddQuestion(question, type)
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
            onConfirm = { newQuestion, type ->
                onEditQuestion(question.id, newQuestion, type)
                showEditQuestionDialog = null
            }
        )
    }

    // Delete Question Dialog
    showDeleteQuestionDialog?.let { question ->
        AlertDialog(
            onDismissRequest = { showDeleteQuestionDialog = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Delete Question",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${question.question}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.body1
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteQuestion(question.id)
                        showDeleteQuestionDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteQuestionDialog = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CompletedJournalCard(
    question: QuestionAnswerDTO,
    answer: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = getQuestionTypeColor(question.type ?: QuestionType.TEXT
                    ).copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getQuestionTypeIcon(question.type?: QuestionType.TEXT),
                            contentDescription = question.type?.name,
                            tint = getQuestionTypeColor(question.type ?: QuestionType.TEXT),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
            }
            
            when (question.type) {
                QuestionType.TEXT -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colors.background,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
                        )
                    ) {
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colors.onSurface
                        )
                    }
                }
                QuestionType.MOOD -> {
                    if (answer.isNotBlank()) {
                        val mood = try {
                            MoodType.valueOf(answer)
                        } catch (e: IllegalArgumentException) {
                            MoodType.NEUTRAL
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = getMoodCategoryColor(mood.category).copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                getMoodCategoryColor(mood.category).copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mood.emoji,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = mood.label,
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                    Text(
                                        text = mood.category,
                                        style = MaterialTheme.typography.caption,
                                        color = getMoodCategoryColor(mood.category)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No mood selected",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    Text(
                        text = "Unsupported question type",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    onAddQuestion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = "Empty Journal",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Questions Yet",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add your first question to start journaling. You can create text questions or mood tracking questions.",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onAddQuestion,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Question", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Question")
        }
    }
}

@Composable
fun getQuestionTypeColor(type: QuestionType): Color {
    return when (type) {
        QuestionType.TEXT -> Color(0xFF2196F3) // Blue
        QuestionType.MOOD -> Color(0xFFE91E63) // Pink
    }
}

@Composable
fun getQuestionTypeIcon(type: QuestionType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        QuestionType.TEXT -> Icons.Default.Edit
        QuestionType.MOOD -> Icons.Default.Psychology
    }
}

@Composable
fun getMoodCategoryColor(category: String): Color {
    return when (category) {
        "Positive" -> Color(0xFF4CAF50) // Green
        "Neutral" -> Color(0xFF9E9E9E)  // Grey
        "Negative" -> Color(0xFFF44336) // Red
        "Physical" -> Color(0xFFFF9800) // Orange
        "Complex" -> Color(0xFF9C27B0)  // Purple
        else -> MaterialTheme.colors.primary
    }
}
