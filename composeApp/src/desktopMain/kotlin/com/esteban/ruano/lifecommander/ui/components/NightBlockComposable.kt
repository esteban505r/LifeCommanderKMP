package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lifecommander.models.Habit
import kotlinx.coroutines.launch
import services.NightBlockService
import services.dailyjournal.models.QuestionDTO
import services.dailyjournal.models.QuestionType
import ui.viewmodels.DailyJournalViewModel

@Composable
fun NightBlockComposable(
    nightBlockService: NightBlockService,
    habits: List<Habit>,
    onOverride: (String) -> Unit,
    modifier: Modifier = Modifier,
    dailyJournalViewModel: DailyJournalViewModel,
) {
    val isNightBlockActive by nightBlockService.isNightBlockActive.collectAsState()
    val nightBlockTime by nightBlockService.nightBlockTime.collectAsState()
    val lastOverrideReason by nightBlockService.lastOverrideReason.collectAsState()
    val whitelistedHabits by nightBlockService.whitelistedHabits.collectAsState()
    var showWhitelistModal by remember { mutableStateOf(false) }
    var showQuestionsModal by remember { mutableStateOf(false) }
    var showTimePickerModal by remember { mutableStateOf(false) }
    var newQuestionText by remember { mutableStateOf("") }
    var editingQuestion by remember { mutableStateOf<QuestionDTO?>(null) }
    var selectedHour by remember { mutableStateOf(nightBlockTime.hour) }
    var selectedMinute by remember { mutableStateOf(nightBlockTime.minute) }

    val state by dailyJournalViewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Load questions when the questions modal is shown
    LaunchedEffect(showQuestionsModal) {
        if (showQuestionsModal) {
            dailyJournalViewModel.loadQuestions()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = if (isNightBlockActive) MaterialTheme.colors.error.copy(alpha = 0.1f) 
                         else MaterialTheme.colors.primary.copy(alpha = 0.1f),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isNightBlockActive) Icons.Default.Nightlight 
                                    else Icons.Default.LightMode,
                        contentDescription = "Night Block Status",
                        tint = if (isNightBlockActive) MaterialTheme.colors.error 
                              else MaterialTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Text(
                        text = if (isNightBlockActive) "Night Block Active" 
                              else "Night Block Inactive",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = if (isNightBlockActive) MaterialTheme.colors.error 
                               else MaterialTheme.colors.primary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showQuestionsModal = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.QuestionAnswer,
                            contentDescription = "Manage Questions",
                            tint = MaterialTheme.colors.primary
                        )
                    }

                    IconButton(
                        onClick = { showWhitelistModal = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Manage Whitelist",
                            tint = MaterialTheme.colors.primary
                        )
                    }

                    IconButton(
                        onClick = { showTimePickerModal = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Set Time",
                            tint = MaterialTheme.colors.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                nightBlockService.toggleNightBlock()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isNightBlockActive) Icons.Default.Pause
                                        else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Night Block",
                            tint = if (isNightBlockActive) MaterialTheme.colors.error 
                                  else MaterialTheme.colors.primary
                        )
                    }
                }
            }

            // Status Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colors.surface.copy(alpha = 0.1f)),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isNightBlockActive) "Night Block is active until tomorrow morning"
                              else "Scheduled for ${nightBlockTime.hour}:${nightBlockTime.minute}",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )

                    if (isNightBlockActive) {
                        Button(
                            onClick = { onOverride("") },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Override Night Block")
                        }
                    }
                }
            }

            // Whitelist Summary
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colors.surface.copy(alpha = 0.1f)),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Whitelisted Habits",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Text(
                            text = "${whitelistedHabits.size} habits",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (lastOverrideReason != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colors.surface.copy(alpha = 0.1f)),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Last Override Reason:",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface
                        )
                        Text(
                            text = lastOverrideReason ?: "",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface
                        )
                    }
                }
            }
        }
    }

    if (showWhitelistModal) {
        Dialog(
            onDismissRequest = { showWhitelistModal = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colors.surface,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Manage Whitelisted Habits",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        IconButton(
                            onClick = { showWhitelistModal = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Select habits that are allowed during Night Block",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(habits) { habit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = habit?.name?: "Unknown Habit",
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Switch(
                                    checked = whitelistedHabits.contains(habit.id),
                                    onCheckedChange = {
                                        coroutineScope.launch {
                                            nightBlockService.toggleWhitelistedHabit(habit.id)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colors.primary,
                                        checkedTrackColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showWhitelistModal = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }

    if (showQuestionsModal) {
        Dialog(
            onDismissRequest = { showQuestionsModal = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colors.surface,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Manage Night Block Questions",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        IconButton(
                            onClick = { showQuestionsModal = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Add or remove reflection questions for Night Block",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )

                    // Add new question section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newQuestionText,
                            onValueChange = { newQuestionText = it },
                            label = { Text("New Question") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newQuestionText.isNotBlank()) {
                                    dailyJournalViewModel.addQuestion(newQuestionText, type = QuestionType.TEXT)
                                    newQuestionText = ""
                                }
                            },
                            enabled = newQuestionText.isNotBlank()
                        ) {
                            Text("Add")
                        }
                    }

                    // Questions list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.questions) { question ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (editingQuestion?.id == question.id) {
                                        OutlinedTextField(
                                            value = editingQuestion?.question ?: "",
                                            onValueChange = { 
                                                editingQuestion = editingQuestion?.copy(question = it)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(
                                                onClick = { editingQuestion = null }
                                            ) {
                                                Text("Cancel")
                                            }
                                            Button(
                                                onClick = {
                                                    editingQuestion?.let { q ->
                                                        dailyJournalViewModel.updateQuestion(q.id, q.question, q.type ?: QuestionType.TEXT)
                                                    }
                                                    editingQuestion = null
                                                }
                                            ) {
                                                Text("Save")
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = question.question,
                                            style = MaterialTheme.typography.body1
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { editingQuestion = question }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit"
                                                )
                                            }
                                            IconButton(
                                                onClick = { dailyJournalViewModel.deleteQuestion(question.id) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showQuestionsModal = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }

    if (showTimePickerModal) {
        Dialog(
            onDismissRequest = { showTimePickerModal = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colors.surface,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Set Night Block Time",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        IconButton(
                            onClick = { showTimePickerModal = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Select the time when Night Block should activate",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour picker
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Hour",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        selectedHour = (selectedHour - 1 + 24) % 24
                                    }
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, "Increase hour")
                                }
                                Text(
                                    text = String.format("%02d", selectedHour),
                                    style = MaterialTheme.typography.h4
                                )
                                IconButton(
                                    onClick = {
                                        selectedHour = (selectedHour + 1) % 24
                                    }
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, "Decrease hour")
                                }
                            }
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.h4
                        )

                        // Minute picker
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Minute",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        selectedMinute = (selectedMinute - 1 + 60) % 60
                                    }
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, "Increase minute")
                                }
                                Text(
                                    text = String.format("%02d", selectedMinute),
                                    style = MaterialTheme.typography.h4
                                )
                                IconButton(
                                    onClick = {
                                        selectedMinute = (selectedMinute + 1) % 60
                                    }
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, "Decrease minute")
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showTimePickerModal = false }
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    nightBlockService.setNightBlockTime(selectedHour, selectedMinute)
                                }
                                showTimePickerModal = false
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
} 