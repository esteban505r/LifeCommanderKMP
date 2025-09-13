package com.esteban.ruano.workout_presentation.ui.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.ExerciseSet

private val CardBackground = Color(0xFFF7F7FA) // Custom light gray for card
private val CardBorder = Color(0xFFE0E0E0)
private val ChipBackground = Color(0xFFF0F0F5)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExerciseCard(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    sets: List<ExerciseSet>,
    workoutDayId: String,
    // Actions
    onUpdate: (Exercise) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onCompleteExercise: (() -> Unit)? = null,
    onAddSet: (repsDone: Int, exerciseId: String, workoutDayId: String, onResult: (ExerciseSet?) -> Unit) -> Unit,
    onUpdateSetReps: (setId: String, newReps: Int) -> Unit,
    onRemoveSet: (setId: String) -> Unit,
    // Flags
    isCompleted: Boolean = false,
    showActionButtons: Boolean = true,
    defaultReps: Int = 0
) {
    // UI state
    var expanded by remember { mutableStateOf(false) }
    var editableSetId by remember { mutableStateOf<String?>(null) }
    var desiredRepsText by remember { mutableStateOf(defaultReps.toString()) }
    var isAdding by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showExpectedSetsDialog by remember { mutableStateOf(false) }
    var pendingReps by remember { mutableStateOf<Int?>(null) }

    val canMutateSets = !isCompleted

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300)),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = CardBackground,
        elevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) MaterialTheme.colors.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colors.primary.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isCompleted) Icons.Filled.FitnessCenter else Icons.Outlined.FitnessCenter,
                            contentDescription = "Exercise",
                            tint = if (isCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                exercise.name,
                                style = MaterialTheme.typography.subtitle1.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                                )
                            )
                            if (isCompleted) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colors.primary,
                                    modifier = Modifier.size(15.dp).padding(start = 4.dp)
                                )
                            }
                        }
                        if (exercise.description?.isEmpty() == false) {
                            Text(
                                exercise.description?:"",
                                style = MaterialTheme.typography.body2.copy(
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                if (showActionButtons) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBorder.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        if (onCompleteExercise != null) {
                            IconButton(
                                onClick = onCompleteExercise,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                    contentDescription = if (isCompleted) "Completed" else "Complete Exercise",
                                    tint = if (isCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { onUpdate(exercise) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colors.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDelete(exercise.id ?: "") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colors.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Stats row
            if ((exercise.baseSets?:0) > 0 || (exercise.baseReps?:0) > 0 || (exercise.restSecs?:0) > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(
                    color = CardBorder.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if ((exercise.baseSets?:0) > 0) {
                        StatChip("${exercise.baseSets}", "sets", ChipBackground, MaterialTheme.colors.primary)
                    }
                    if ((exercise.baseReps?:0) > 0) {
                        StatChip("${exercise.baseReps}", "reps", ChipBackground, MaterialTheme.colors.secondary)
                    }
                    if ((exercise.restSecs?:0) > 0) {
                        StatChip("${exercise.restSecs}", "rest", ChipBackground, MaterialTheme.colors.error)
                    }
                }
            }

            // Sets accordion
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder.copy(alpha = 0.5f))
            ) {
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                Spacer(Modifier.width(8.dp))
                Text("Sets (${sets.size})")
            }

            if (expanded) {
                if (isCompleted) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.05f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
                        Spacer(Modifier.width(6.dp))
                        Text("Exercise completed — sets are locked.", style = MaterialTheme.typography.caption)
                    }
                }

                Spacer(Modifier.height(6.dp))

                // List
                sets.forEachIndexed { index, set ->
                    var editText by remember(set.id) { mutableStateOf(set.reps.toString()) }
                    val isEditable = editableSetId == set.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isEditable) MaterialTheme.colors.primary.copy(alpha = 0.08f)
                                else MaterialTheme.colors.onSurface.copy(alpha = 0.035f)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Set ${index + 1}", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(10.dp))

                        if (isEditable && canMutateSets) {
                            TextField(
                                value = editText,
                                onValueChange = { v -> editText = v.filter { it.isDigit() }.take(4) },
                                singleLine = true,
                                modifier = Modifier.width(88.dp),
                                textStyle = MaterialTheme.typography.body2,
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Color.Transparent,
                                    focusedIndicatorColor = MaterialTheme.colors.primary,
                                    unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                    textColor = MaterialTheme.colors.onSurface
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("reps", style = MaterialTheme.typography.caption)
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = {
                                editText.toIntOrNull()?.let {
                                    onUpdateSetReps(set.id, it)
                                    editableSetId = null
                                    errorMsg = null
                                } ?: run { errorMsg = "Enter a valid number of reps." }
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Save")
                            }
                        } else {
                            Text("${set.reps} reps", style = MaterialTheme.typography.body2)
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = { if (canMutateSets) onRemoveSet(set.id) },
                                enabled = canMutateSets
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = if (canMutateSets) MaterialTheme.colors.error
                                    else MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }

                if (errorMsg != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(errorMsg!!, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                }

                // Add set
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = desiredRepsText,
                        onValueChange = { desiredRepsText = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Reps") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        enabled = canMutateSets && !isAdding
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!canMutateSets) return@Button
                            val reps = desiredRepsText.toIntOrNull()
                            if (reps == null) {
                                errorMsg = "Enter a valid number of reps."
                                return@Button
                            }
                            val expected = exercise.baseSets
                            if ((expected?:0) > 0 && sets.size >= (expected?:0)) {
                                pendingReps = reps
                                showExpectedSetsDialog = true
                                return@Button
                            }
                            isAdding = true
                            errorMsg = null
                            onAddSet(reps, exercise.id ?: return@Button, workoutDayId) { newSet ->
                                isAdding = false
                                if (newSet != null) {
                                    editableSetId = newSet.id
                                    desiredRepsText = defaultReps.toString()
                                } else {
                                    errorMsg = "Could not add set. Try again."
                                }
                            }
                        },
                        enabled = canMutateSets && !isAdding
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Add")
                        }
                    }
                }
            }
        }
    }

    // Expected sets warning
    if (showExpectedSetsDialog) {
        AlertDialog(
            onDismissRequest = { showExpectedSetsDialog = false; pendingReps = null },
            title = { Text("Expected sets reached") },
            text = {
                Text("You’ve already completed ${exercise.baseSets} set(s) for ${exercise.name}. Add another?")
            },
            confirmButton = {
                TextButton(onClick = {
                    val reps = pendingReps ?: return@TextButton
                    showExpectedSetsDialog = false
                    pendingReps = null
                    // proceed
                    isAdding = true
                    errorMsg = null
                    onAddSet(reps, exercise.id ?: return@TextButton, workoutDayId) { newSet ->
                        isAdding = false
                        if (newSet != null) {
                            editableSetId = newSet.id
                            desiredRepsText = defaultReps.toString()
                        } else {
                            errorMsg = "Could not add set. Try again."
                        }
                    }
                }) { Text("Add anyway") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExpectedSetsDialog = false; pendingReps = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun StatChip(
    text: String,
    label: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.subtitle2.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.caption.copy(
                    color = textColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
