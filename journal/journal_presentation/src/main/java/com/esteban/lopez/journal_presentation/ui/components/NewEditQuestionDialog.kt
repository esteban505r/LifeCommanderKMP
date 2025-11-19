package com.esteban.ruano.journal_presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import services.dailyjournal.models.QuestionType

@Composable
fun NewEditQuestionDialog(
    show: Boolean,
    title: String,
    initialQuestion: String,
    existingQuestions: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, QuestionType) -> Unit
) {
    if (show) {
        var questionText by remember { mutableStateOf(initialQuestion) }
        var selectedType by remember { mutableStateOf(QuestionType.TEXT) }
        var showError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        LaunchedEffect(initialQuestion) {
            questionText = initialQuestion
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .shadow(16.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                elevation = 0.dp,
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colors.primary,
                                        MaterialTheme.colors.primary.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(24.dp)
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
                                    color = MaterialTheme.colors.onPrimary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (initialQuestion.isBlank()) Icons.Default.Add else Icons.Default.Edit,
                                            contentDescription = "Question",
                                            tint = MaterialTheme.colors.onPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.h5,
                                        color = MaterialTheme.colors.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (initialQuestion.isBlank()) "Add a new question to your journal" else "Edit your journal question",
                                        style = MaterialTheme.typography.body1,
                                        color = MaterialTheme.colors.onPrimary.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.onPrimary.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colors.onPrimary
                                )
                            }
                        }
                    }

                    // Content
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Question Type Selector
                        Column {
                            Text(
                                text = "Question Type",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QuestionType.values().forEach { type ->
                                    QuestionTypeCard(
                                        type = type,
                                        isSelected = selectedType == type,
                                        onClick = { selectedType = type }
                                    )
                                }
                            }
                        }

                        // Question Text Input
                        Column {
                            Text(
                                text = "Question",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            OutlinedTextField(
                                value = questionText,
                                onValueChange = {
                                    questionText = it
                                    showError = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        when (selectedType) {
                                            QuestionType.TEXT -> "e.g., What did you accomplish today?"
                                            QuestionType.MOOD -> "e.g., How are you feeling right now?"
                                        }
                                    )
                                },
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = MaterialTheme.colors.surface,
                                    focusedIndicatorColor = MaterialTheme.colors.primary,
                                    unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                    cursorColor = MaterialTheme.colors.primary,
                                    textColor = MaterialTheme.colors.onSurface
                                )
                            )
                        }

                        // Error Message
                        AnimatedVisibility(
                            visible = showError,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colors.error.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colors.error.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colors.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage,
                                        color = MaterialTheme.colors.error,
                                        style = MaterialTheme.typography.body2
                                    )
                                }
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel")
                            }
                            
                            Button(
                                onClick = {
                                    when {
                                        questionText.isBlank() -> {
                                            showError = true
                                            errorMessage = "Question cannot be empty"
                                        }
                                        existingQuestions.contains(questionText) -> {
                                            showError = true
                                            errorMessage = "This question already exists"
                                        }
                                        else -> {
                                            onConfirm(questionText, selectedType)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                )
                            ) {
                                Icon(
                                    if (initialQuestion.isBlank()) Icons.Default.Add else Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (initialQuestion.isBlank()) "Add Question" else "Update Question")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.QuestionTypeCard(
    type: QuestionType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = 200)
    )
    
    Card(
        modifier = Modifier
            .weight(1f)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(elevation, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
        backgroundColor = if (isSelected) {
            getQuestionTypeColor(type).copy(alpha = 0.1f)
        } else {
            MaterialTheme.colors.surface
        },
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                getQuestionTypeColor(type)
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) {
                    getQuestionTypeColor(type)
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getQuestionTypeIcon(type),
                        contentDescription = type.name,
                        tint = if (isSelected) Color.White else getQuestionTypeColor(type),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (type) {
                    QuestionType.TEXT -> "Text"
                    QuestionType.MOOD -> "Mood"
                },
                style = MaterialTheme.typography.body2,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    getQuestionTypeColor(type)
                } else {
                    MaterialTheme.colors.onSurface
                },
                textAlign = TextAlign.Center
            )
            
            Text(
                text = when (type) {
                    QuestionType.TEXT -> "Free text response"
                    QuestionType.MOOD -> "Emoji selection"
                },
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

