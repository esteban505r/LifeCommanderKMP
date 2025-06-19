package ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.esteban.ruano.lifecommander.models.Exercise
import java.awt.Dimension

@Composable
fun NewEditExerciseDialog(
    exerciseToEdit: Exercise?,
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit
) {
    var name by remember { mutableStateOf(exerciseToEdit?.name ?: "") }
    var description by remember { mutableStateOf(exerciseToEdit?.description ?: "") }
    var restSecs by remember { mutableStateOf(exerciseToEdit?.restSecs?.toString() ?: "60") }
    var baseSets by remember { mutableStateOf(exerciseToEdit?.baseSets?.toString() ?: "3") }
    var baseReps by remember { mutableStateOf(exerciseToEdit?.baseReps?.toString() ?: "10") }
    var muscleGroup by remember { mutableStateOf(exerciseToEdit?.muscleGroup ?: "FULL_BODY") }
//    var equipment by remember { mutableStateOf(exerciseToEdit?.equipment?.joinToString(", ") ?: "") }
//    var resource by remember { mutableStateOf(exerciseToEdit?.resource ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    val muscleGroups = listOf("FULL_BODY", "CORE", "LEGS", "UPPER_BODY", "LOWER_BODY", "ARMS", "BACK", "CHEST", "SHOULDERS")

    if (show) {
        DialogWindow(
            onCloseRequest = onDismiss,
            state = rememberDialogState(position = WindowPosition(Alignment.Center))
        ) {
            LaunchedEffect(Unit) {
                window.size = Dimension(600, 650)
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
                                        text = if (exerciseToEdit != null) "Edit Exercise" else "Create New Exercise",
                                        style = MaterialTheme.typography.h6,
                                        color = MaterialTheme.colors.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (exerciseToEdit != null) "Update your exercise details" else "Set up a new exercise",
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
                            // Name
                            Column {
                                Text("Name", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    placeholder = { Text("e.g., Bench Press") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    ),
                                    leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) }
                                )
                            }
                            // Description
                            Column {
                                Text("Description", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    placeholder = { Text("e.g., Lay down on a bench and press the bar up") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    )
                                )
                            }
                            // Rest Seconds
                            Column {
                                Text("Rest Seconds", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = restSecs,
                                    onValueChange = { restSecs = it.filter { c -> c.isDigit() } },
                                    placeholder = { Text("e.g., 60") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    ),
                                    leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null) }
                                )
                            }
                            // Base Sets
                            Column {
                                Text("Base Sets", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = baseSets,
                                    onValueChange = { baseSets = it.filter { c -> c.isDigit() } },
                                    placeholder = { Text("e.g., 3") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    ),
                                    leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
                                )
                            }
                            // Base Reps
                            Column {
                                Text("Base Reps", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = baseReps,
                                    onValueChange = { baseReps = it.filter { c -> c.isDigit() } },
                                    placeholder = { Text("e.g., 10") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    ),
                                    leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
                                )
                            }
                            // Muscle Group
                            Column {
                                Text("Muscle Group", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                DropdownMenuBox(
                                    options = muscleGroups,
                                    selectedOption = muscleGroup,
                                    onOptionSelected = { muscleGroup = it }
                                )
                            }
                            // Equipment
                           /* Column {
                                Text("Equipment (comma separated)", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = equipment,
                                    onValueChange = { equipment = it },
                                    placeholder = { Text("e.g., Barbell, Bench") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    ),
                                    leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
                                )
                            }*/
                            // Resource (optional)
                           /* Column {
                                Text("Resource (optional)", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = resource,
                                    onValueChange = { resource = it },
                                    placeholder = { Text("e.g., https://... or ID") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    )
                                )
                            }*/
                            if (error != null) {
                                Text(error!!, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
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
                                    if (name.isBlank() || restSecs.isBlank() || baseSets.isBlank() || baseReps.isBlank() || muscleGroup.isBlank()) {
                                        error = "Please fill in all required fields."
                                        return@Button
                                    }
                                    error = null
                                    onSave(
                                        Exercise(
                                            id = exerciseToEdit?.id ?: "",
                                            name = name,
                                            description = description,
                                            restSecs = restSecs.toIntOrNull() ?: 60,
                                            baseSets = baseSets.toIntOrNull() ?: 3,
                                            baseReps = baseReps.toIntOrNull() ?: 10,
                                            muscleGroup = muscleGroup,
//                                             equipment = equipment.split(",").map { it.trim() }.filter { it.isNotEmpty() },
//                                            resource = resource
                                        )
                                    )
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                )
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (exerciseToEdit != null) "Update Exercise" else "Create Exercise")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.primary)
        ) {
            Text(selectedOption)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onOptionSelected(option)
                    expanded = false
                }) {
                    Text(option)
                }
            }
        }
    }
} 