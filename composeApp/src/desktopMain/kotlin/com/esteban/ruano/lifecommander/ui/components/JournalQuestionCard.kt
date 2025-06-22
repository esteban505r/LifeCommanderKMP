package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import services.dailyjournal.models.MoodType
import services.dailyjournal.models.QuestionDTO
import services.dailyjournal.models.QuestionType
import java.util.Locale

@Composable
fun JournalQuestionCard(
    question: QuestionDTO,
    answer: String?,
    onAnswerChanged: (String) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with question info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Question type icon
                    Surface(
                        shape = CircleShape,
                        color = getQuestionTypeColor(question.type ?: QuestionType.TEXT
                        ).copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getQuestionTypeIcon(question.type ?: QuestionType.TEXT
                                ),
                                contentDescription = question.type?.name ?: "Question Type",
                                tint = getQuestionTypeColor(question.type ?: QuestionType.TEXT),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Text(
                            text = question.type?.name?.lowercase()?.capitalize(Locale.ROOT) ?: "Text",
                            style = MaterialTheme.typography.caption,
                            color = getQuestionTypeColor(question.type ?: QuestionType.TEXT),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit question",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete question",
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }
            
            // Answer section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    when (question.type) {
                        QuestionType.TEXT -> {
                            TextInputSection(
                                answer = answer,
                                onAnswerChanged = onAnswerChanged
                            )
                        }
                        QuestionType.MOOD -> {
                            MoodInputSection(
                                answer = answer,
                                onAnswerChanged = onAnswerChanged
                            )
                        }

                        null -> {
                            Text(
                                text = "Unsupported question type",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            // Answer preview (when collapsed)
            if (!isExpanded && answer != null && answer.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AnswerPreview(
                    question = question,
                    answer = answer
                )
            }
        }
    }
}

@Composable
fun TextInputSection(
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = answer ?: "",
        onValueChange = onAnswerChanged,
        label = { Text("Your answer") },
        placeholder = { Text("Type your thoughts here...") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 6,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.surface,
            focusedIndicatorColor = MaterialTheme.colors.primary,
            unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
            cursorColor = MaterialTheme.colors.primary,
            textColor = MaterialTheme.colors.onSurface,
            placeholderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f
        )
        )
    )
}

@Composable
fun MoodInputSection(
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {
    val selectedMood = answer?.let { moodString ->
        try {
            MoodType.valueOf(moodString)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
    
    Column {
        Text(
            text = "Select your mood:",
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        MoodSelector(
            selectedMood = selectedMood,
            onMoodSelected = { mood ->
                onAnswerChanged(mood.name)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AnswerPreview(
    question: QuestionDTO,
    answer: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colors.background,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Answered",
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            when (question.type) {
                QuestionType.TEXT -> {
                    Text(
                        text = answer.take(50) + if (answer.length > 50) "..." else "",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }
                QuestionType.MOOD -> {
                    val mood = try {
                        MoodType.valueOf(answer)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mood?.emoji ?: "😐",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mood?.label ?: "Unknown",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                null -> {
                    Text(
                        text = "Unsupported question type",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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