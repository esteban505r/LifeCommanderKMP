package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifecommander.models.StudyItem
import com.lifecommander.models.StudyTopic
import ui.theme.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StudyItemFormModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    item: StudyItem?,
    topics: List<StudyTopic>,
    onSave: (StudyItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(item?.title ?: "") }
    var topicId by remember { mutableStateOf(item?.topic?.id ?: "") } // Extract ID from topic object
    var obsidianPath by remember { mutableStateOf(item?.obsidianPath ?: "") }
    var stage by remember { mutableStateOf(item?.stage ?: "PENDING") }
    var modeHint by remember { mutableStateOf(item?.modeHint ?: "") }
    var discipline by remember { mutableStateOf(item?.discipline ?: "") }
    var progress by remember { mutableStateOf(item?.progress?.toString() ?: "0") }
    var estimatedEffort by remember { mutableStateOf(item?.estimatedEffortMinutes?.toString() ?: "") }

    ModernModal(
        isVisible = isVisible,
        onDismiss = onDismiss,
        title = if (item == null) "New Study Item" else "Edit Study Item",
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

            Button(
                onClick = {
                    // Create StudyItem with topic object if topicId is provided
                    val selectedTopic = topics.find { it.id == topicId }
                    onSave(
                        StudyItem(
                            id = item?.id ?: "",
                            topic = selectedTopic, // Set topic object instead of topicId
                            title = title,
                            obsidianPath = obsidianPath.ifBlank { null },
                            stage = stage,
                            modeHint = modeHint.ifBlank { null },
                            discipline = discipline.ifBlank { null },
                            progress = progress.toIntOrNull()?.coerceIn(0, 100) ?: 0,
                            estimatedEffortMinutes = estimatedEffort.toIntOrNull()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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

            OutlinedTextField(
                value = obsidianPath,
                onValueChange = { obsidianPath = it },
                label = { Text("Obsidian Path") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            var expandedStage by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedStage,
                onExpandedChange = { expandedStage = !expandedStage },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = stage,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Stage") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStage) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedStage,
                    onDismissRequest = { expandedStage = false }
                ) {
                    listOf("PENDING", "IN_PROGRESS", "PROCESSED").forEach { s ->
                        DropdownMenuItem(onClick = {
                            stage = s
                            expandedStage = false
                        }) {
                            Text(s)
                        }
                    }
                }
            }

            var expandedMode by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedMode,
                onExpandedChange = { expandedMode = !expandedMode },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = modeHint,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mode Hint") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedMode,
                    onDismissRequest = { expandedMode = false }
                ) {
                    DropdownMenuItem(onClick = {
                        modeHint = ""
                        expandedMode = false
                    }) {
                        Text("None")
                    }
                    listOf("INPUT", "PROCESSING", "REVIEW").forEach { m ->
                        DropdownMenuItem(onClick = {
                            modeHint = m
                            expandedMode = false
                        }) {
                            Text(m)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = progress,
                onValueChange = { if (it.all { char -> char.isDigit() }) progress = it },
                label = { Text("Progress (0-100)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = estimatedEffort,
                onValueChange = { if (it.all { char -> char.isDigit() }) estimatedEffort = it },
                label = { Text("Estimated Effort (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

