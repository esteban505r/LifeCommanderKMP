package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifecommander.models.StudyTopic
import com.esteban.ruano.lifecommander.ui.components.ColorPicker
import com.esteban.ruano.lifecommander.ui.components.IconPicker
import ui.theme.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StudyTopicFormModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    topic: StudyTopic?,
    topics: List<StudyTopic>,
    disciplines: List<String> = emptyList(),
    onSave: (StudyTopic) -> Unit,
    onIconUpload: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(topic?.name ?: "") }
    var description by remember { mutableStateOf(topic?.description ?: "") }
    var discipline by remember { mutableStateOf(topic?.discipline ?: "") }
    var disciplineExpanded by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(topic?.color) }
    var icon by remember { mutableStateOf(topic?.icon) }
    var isActive by remember { mutableStateOf(topic?.isActive ?: true) }

    LaunchedEffect(topic) {
        name = topic?.name ?: ""
        description = topic?.description ?: ""
        discipline = topic?.discipline ?: ""
        color = topic?.color
        icon = topic?.icon
        isActive = topic?.isActive ?: true
    }

    ModernModal(
        isVisible = isVisible,
        onDismiss = onDismiss,
        title = if (topic == null) "New Study Topic" else "Edit Study Topic",
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
                    onSave(
                        StudyTopic(
                            id = topic?.id ?: "",
                            name = name,
                            description = description.ifBlank { null },
                            discipline = discipline.ifBlank { null },
                            color = color,
                            icon = icon,
                            isActive = isActive
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank()
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Discipline Autocomplete
            ExposedDropdownMenuBox(
                expanded = disciplineExpanded,
                onExpandedChange = { disciplineExpanded = it }
            ) {
                OutlinedTextField(
                    value = discipline,
                    onValueChange = { discipline = it },
                    label = { Text("Discipline") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = disciplineExpanded)
                    },
                    readOnly = false
                )

                ExposedDropdownMenu(
                    expanded = disciplineExpanded,
                    onDismissRequest = { disciplineExpanded = false }
                ) {
                    // Show existing disciplines that match the input
                    val filteredDisciplines = if (discipline.isBlank()) {
                        disciplines
                    } else {
                        disciplines.filter { it.contains(discipline, ignoreCase = true) }
                    }

                    filteredDisciplines.forEach { disc ->
                        DropdownMenuItem(
                            onClick = {
                                discipline = disc
                                disciplineExpanded = false
                            }
                        ) {
                            Text(disc)
                        }
                    }

                    // Option to create new discipline if input doesn't match
                    if (discipline.isNotBlank() && !filteredDisciplines.contains(discipline)) {
                        Divider()
                        DropdownMenuItem(
                            onClick = {
                                disciplineExpanded = false
                            }
                        ) {
                            Text("Use \"$discipline\"")
                        }
                    }
                }
            }

            // Color Picker
            ColorPicker(
                selectedColor = color,
                onColorSelected = { color = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Icon Picker
            IconPicker(
                selectedIconUrl = icon,
                onIconSelected = { icon = it },
                onIconUploaded = onIconUpload,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Active")
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }
        }
    }
}

