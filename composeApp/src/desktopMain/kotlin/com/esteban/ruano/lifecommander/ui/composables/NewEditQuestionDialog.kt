package ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import java.awt.Dimension

@Composable
fun NewEditQuestionDialog(
    show: Boolean,
    title: String,
    initialQuestion: String,
    existingQuestions: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var question by remember { mutableStateOf(initialQuestion) }
    var error by remember { mutableStateOf<String?>(null) }

    if (show) {
        DialogWindow(
            onCloseRequest = onDismiss,
            state = rememberDialogState(position = WindowPosition(Alignment.Center))
        ) {
            LaunchedEffect(Unit) {
                window.size = Dimension(600, 400)
            }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                color = Color.Transparent
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 8.dp,
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()).padding(0.dp)
                    ) {
                        // Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.primary)
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.h6,
                                        color = MaterialTheme.colors.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (initialQuestion.isBlank()) "Add a new question to your journal" else "Edit your journal question",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onPrimary.copy(alpha = 0.8f)
                                    )
                                }
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(32.dp)
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
                            Column {
                                Text("Question", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = question,
                                    onValueChange = {
                                        question = it
                                        error = when {
                                            it.isBlank() -> "Question cannot be empty"
                                            existingQuestions.contains(it) -> "This question already exists"
                                            else -> null
                                        }
                                    },
                                    placeholder = { Text("e.g., What went well today?") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    )
                                )
                                if (error != null) {
                                    Text(error!!, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                                }
                            }
                        }
                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    if (question.isBlank() || error != null) return@Button
                                    onConfirm(question)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                ),
                                enabled = question.isNotBlank() && error == null
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (initialQuestion.isBlank()) "Add Question" else "Update Question")
                            }
                        }
                    }
                }
            }
        }
    }
} 