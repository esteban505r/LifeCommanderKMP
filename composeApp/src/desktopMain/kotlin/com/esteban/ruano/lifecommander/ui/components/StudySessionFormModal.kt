package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.models.StudyItem
import com.lifecommander.models.StudySession
import com.lifecommander.models.StudyTopic
import kotlinx.datetime.TimeZone
import ui.theme.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StudySessionFormModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    session: StudySession?,
    topics: List<StudyTopic>,
    items: List<StudyItem>,
    onSave: (StudySession) -> Unit,
    onComplete: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(session?.mode ?: "INPUT") }
    var topicId by remember { mutableStateOf(session?.topic?.id ?: "") } // Extract ID from topic object
    var studyItemId by remember { mutableStateOf(session?.studyItem?.id ?: "") } // Extract ID from studyItem object
    var notes by remember { mutableStateOf(session?.notes ?: "") }
    val now = getCurrentDateTime(TimeZone.currentSystemDefault())
    var actualStart by remember { mutableStateOf(session?.actualStart ?: now.formatDefault()) }

    val isActive = session?.actualEnd == null

    ModernModal(
        isVisible = isVisible,
        onDismiss = onDismiss,
        title = if (session == null) "New Study Session" else "Edit Study Session",
        modifier = modifier,
        actions = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isActive && session != null) {
                Button(
                    onClick = {
                        val endTime = getCurrentDateTime(TimeZone.currentSystemDefault()).formatDefault()
                        onComplete(session.id, endTime, notes.ifBlank { null })
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Complete Session")
                }
            } else {
                Button(
                    onClick = {
                        // Create StudySession with topic and studyItem objects if IDs are provided
                        val selectedTopic = topics.find { it.id == topicId }
                        val selectedItem = items.find { it.id == studyItemId }
                        onSave(
                            StudySession(
                                id = session?.id ?: "",
                                topic = selectedTopic, // Set topic object instead of topicId
                                studyItem = selectedItem, // Set studyItem object instead of studyItemId
                                mode = mode,
                                actualStart = actualStart,
                                notes = notes.ifBlank { null }
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = mode.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var expandedMode by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedMode,
                onExpandedChange = { expandedMode = !expandedMode },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = mode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedMode,
                    onDismissRequest = { expandedMode = false }
                ) {
                    listOf("INPUT", "PROCESSING", "REVIEW").forEach { m ->
                        DropdownMenuItem(onClick = {
                            mode = m
                            expandedMode = false
                        }) {
                            Text(m)
                        }
                    }
                }
            }

            var expandedTopic by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedTopic,
                onExpandedChange = { expandedTopic = !expandedTopic },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = topics.find { it.id == topicId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Topic") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTopic) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedTopic,
                    onDismissRequest = { expandedTopic = false }
                ) {
                    DropdownMenuItem(onClick = {
                        topicId = ""
                        expandedTopic = false
                    }) {
                        Text("None")
                    }
                    topics.forEach { topic ->
                        DropdownMenuItem(onClick = {
                            topicId = topic.id
                            expandedTopic = false
                        }) {
                            Text(topic.name)
                        }
                    }
                }
            }

            var expandedItem by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedItem,
                onExpandedChange = { expandedItem = !expandedItem },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = items.find { it.id == studyItemId }?.title ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Study Item") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedItem) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedItem,
                    onDismissRequest = { expandedItem = false }
                ) {
                    DropdownMenuItem(onClick = {
                        studyItemId = ""
                        expandedItem = false
                    }) {
                        Text("None")
                    }
                    items.filter { topicId.isEmpty() || it.topic?.id == topicId }.forEach { item ->
                        DropdownMenuItem(onClick = {
                            studyItemId = item.id
                            expandedItem = false
                        }) {
                            Text(item.title)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = actualStart,
                onValueChange = { actualStart = it },
                label = { Text("Start Time") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            if (session != null && !isActive) {
                session.durationMinutes?.let {
                    Text(
                        text = "Duration: ${it} minutes",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}

