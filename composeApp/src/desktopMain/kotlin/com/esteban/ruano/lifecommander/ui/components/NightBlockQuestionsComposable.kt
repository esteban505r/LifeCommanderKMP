package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ui.viewmodels.DailyJournalViewModel
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun NightBlockQuestionsComposable(
    viewModel: DailyJournalViewModel,
    currentTime: LocalDateTime = LocalDateTime.now()
) {
    var timeToSleep by remember { mutableStateOf(0) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var answer by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadQuestions()

        while (true) {
            delay(1.seconds)
            timeToSleep++
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer Section
            Text(
                text = "Time since Night Block started",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${timeToSleep / 60}:${String.format("%02d", timeToSleep % 60)}",
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Questions Section
            AnimatedVisibility(
                visible = state.showQuestions && !state.isLoading && state.error == null && !state.isCompleted,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reflection Questions",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (state.questions.isNotEmpty()) {
                        Text(
                            text = state.questions[currentQuestionIndex].question,
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        OutlinedTextField(
                            value = answer,
                            onValueChange = { answer = it },
                            label = { Text("Your answer") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            singleLine = false,
                            maxLines = 3
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentQuestionIndex > 0) {
                                        currentQuestionIndex--
                                        answer = ""
                                    }
                                },
                                enabled = currentQuestionIndex > 0
                            ) {
                                Icon(Icons.Default.ArrowBack, "Previous question")
                            }

                            IconButton(
                                onClick = {
                                    if (currentQuestionIndex < state.questions.size - 1) {
                                        viewModel.addAnswer(
                                            state.questions[currentQuestionIndex].id,
                                            answer
                                        )
                                        currentQuestionIndex++
                                        answer = ""
                                    } else {
                                        viewModel.addAnswer(
                                            state.questions[currentQuestionIndex].id,
                                            answer
                                        )
                                        viewModel.completeDailyJournal()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ArrowForward, "Next question")
                            }
                        }
                    }
                }
            }

            // Loading State
            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CircularProgressIndicator()
            }

            // Error State
            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body1
                    )
                    Button(
                        onClick = { viewModel.resetError() }
                    ) {
                        Text("Try Again")
                    }
                }
            }

            // Completion Message
            AnimatedVisibility(
                visible = !(state.showQuestions) && !state.isLoading && state.error == null && state.isCompleted,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = "Good night",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Time to sleep!",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.primary
                    )
                    Text(
                        text = "Sweet dreams!",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }
        }
    }
} 