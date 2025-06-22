package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.ui.state.WorkoutState
import com.esteban.ruano.ui.LifeCommanderDesignSystem
import ui.composables.NewEditExerciseDialog

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WorkoutScreen(
    state: WorkoutState,
    onAdd: (Exercise) -> Unit,
    onUpdate: (Exercise) -> Unit,
    onDelete: (String) -> Unit,
    onDaySelected: (Int) -> Unit,
    onCompleteWorkout: (String) -> Unit = {},
    onCompleteExercise: (String, String) -> Unit = { _, _ -> },
    onChangeAllExercisesMode: (Boolean) -> Unit = {},
    onGetAllExercises: () -> Unit = {},
    onBindExerciseToDay: (String, Int, (Boolean) -> Unit) -> Unit = { _, _, _ -> },
    onUnbindExerciseFromDay: (String, Int, (Boolean) -> Unit) -> Unit = { _, _, _ -> }
) {
    var showExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var showBindDialog by remember { mutableStateOf<Exercise?>(null) }
    val allExercisesMode = state.allExerciseMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colors.background,
                        MaterialTheme.colors.surface.copy(alpha = 0.95f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Header section with title and add button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Workout Plan",
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
            )
            Button(
                onClick = { showExerciseDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                ),
                elevation = ButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Exercise",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Add Exercise",
                    style = MaterialTheme.typography.button.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Day selection chips
            Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Day",
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = allExercisesMode,
                        onClick = {
                            onChangeAllExercisesMode(true)
                            onGetAllExercises()
                        },
                        content = { Text("All Exercises") },
                        colors = ChipDefaults.filterChipColors(
                            selectedBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.12f),
                            selectedContentColor = MaterialTheme.colors.primary
                        )
                    )
                    
                    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    dayNames.forEachIndexed { idx, name ->
                        val day = idx + 1
                        FilterChip(
                            selected = (!allExercisesMode && state.daySelected == day),
                            onClick = {
                                onChangeAllExercisesMode(false)
                                onDaySelected(day)
                            },
                            content = { Text(name) },
                            colors = ChipDefaults.filterChipColors(
                                selectedBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.12f),
                                selectedContentColor = MaterialTheme.colors.primary
                            )
                                        )
                                    }
                                }
                            }
                        }

        if (state.exercises.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = 4.dp,
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    Text(
                            if (allExercisesMode) "No exercises in database." else "No exercises for this day.",
                            style = MaterialTheme.typography.h6.copy(
                                color = MaterialTheme.colors.onSurface,
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (!allExercisesMode) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Add some exercises to get started with your workout plan!",
                                style = MaterialTheme.typography.body2.copy(
                                    color = MaterialTheme.colors.onSurface
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                    items(state.exercises.size) { idx ->
                        val exercise = state.exercises[idx]
                    val boundDays = state.exerciseDayMap[exercise.id] ?: emptySet()
                    
                        ExerciseCard(
                            exercise = exercise,
                            onUpdate = {
                                exerciseToEdit = it
                                showExerciseDialog = true
                            },
                        onDelete = onDelete,
                        boundDays = boundDays.toList(),
                        onUnbindDay = { day ->
                            onUnbindExerciseFromDay(exercise.id, day) { success ->
                                if (success) onGetAllExercises()
                            }
                        },
                        onBindToDay = if (allExercisesMode) { { showBindDialog = exercise } } else null,
                        onCompleteExercise = if (!allExercisesMode && state.workoutDays.isNotEmpty()) { { 
                            val workoutDayId = state.workoutDays.firstOrNull()?.id
                            if (workoutDayId != null) {
                                onCompleteExercise(exercise.id, workoutDayId)
                            }
                        } } else null,
                        isAllExercisesMode = allExercisesMode,
                        isCompleted = state.completedExercises.contains(exercise.id)
                    )
                }
            }
        }

        // Dialogs
        NewEditExerciseDialog(
            exerciseToEdit = exerciseToEdit,
            show = showExerciseDialog,
            onDismiss = {
                showExerciseDialog = false
                exerciseToEdit = null
            },
            onSave = { exercise ->
                if (exerciseToEdit == null) {
                    onAdd(exercise)
                } else {
                    onUpdate(exercise)
                }
            }
        )

        showBindDialog?.let { exercise ->
            val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            val allDays = (1..7).toList()
            val initialBoundDays = state.exerciseDayMap[exercise.id] ?: emptySet()
            val selectedDaysState = rememberUpdatedState(initialBoundDays)
            var selectedDays by remember { mutableStateOf(initialBoundDays) }
            LaunchedEffect(initialBoundDays) {
                selectedDays = initialBoundDays
            }

            AlertDialog(
                onDismissRequest = { showBindDialog = null },
                title = {
                    Text(
                        "Schedule '${exercise.name}'",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Select days for this exercise:",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.87f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            dayNames.forEachIndexed { idx, name ->
                                val day = idx + 1
            FilterChip(
                                    selected = selectedDays.contains(day),
                                    onClick = {
                                        selectedDays = if (selectedDays.contains(day)) {
                                            selectedDays - day
                                        } else {
                                            selectedDays + day
                                        }
                                    },
                                    colors = ChipDefaults.filterChipColors(
                                        selectedBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.12f),
                                        selectedContentColor = MaterialTheme.colors.primary
                                    )
                                ) {
                                    Text(name)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    val toBind = selectedDays - initialBoundDays
                    val toUnbind = initialBoundDays - selectedDays
                    toBind.forEach { day ->
                        onBindExerciseToDay(exercise.id, day) { success ->
                            if (success) onGetAllExercises()
                        }
                    }
                    toUnbind.forEach { day ->
                        onUnbindExerciseFromDay(exercise.id, day) { success ->
                            if (success) onGetAllExercises()
                        }
                    }
                    showBindDialog = null
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showBindDialog = null },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExerciseCard(
    exercise: Exercise,
    onUpdate: (Exercise) -> Unit,
    onDelete: (String) -> Unit,
    boundDays: List<Int> = emptyList(),
    onUnbindDay: ((Int) -> Unit)? = null,
    onBindToDay: (() -> Unit)? = null,
    onCompleteExercise: (() -> Unit)? = null,
    isAllExercisesMode: Boolean = false,
    isCompleted: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colors.primary.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            exercise.name,
                            style = MaterialTheme.typography.h6.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onSurface
                            )
                        )
                        exercise.description?.let { desc ->
                            if (desc.isNotBlank()) {
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.body2.copy(
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onUpdate(exercise) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.surface.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { onDelete(exercise.id) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.error.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colors.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Show different button based on mode
                    if (isAllExercisesMode && onBindToDay != null) {
                        // Schedule button for all exercises mode
                        IconButton(
                            onClick = onBindToDay,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colors.secondary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Schedule",
                                tint = MaterialTheme.colors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (!isAllExercisesMode && onCompleteExercise != null) {
                        // Complete exercise button for day mode
                        IconButton(
                            onClick = onCompleteExercise,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCompleted) 
                                        MaterialTheme.colors.primary.copy(alpha = 0.2f)
                                    else 
                                        MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                )
                        ) {
                            Icon(
                                if (isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                contentDescription = if (isCompleted) "Completed" else "Complete Exercise",
                                tint = if (isCompleted) 
                                    MaterialTheme.colors.primary 
                                else 
                                    MaterialTheme.colors.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (exercise.baseSets != null || exercise.baseReps != null || exercise.restSecs != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    exercise.baseSets?.let {
                        Chip(
                            colors = ChipDefaults.chipColors(
                                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                            ),
                            onClick = {}
                        ) {
                            Text(
                                text = "$it sets",
                                style = MaterialTheme.typography.caption.copy(
                                    color = MaterialTheme.colors.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    
                    exercise.baseReps?.let {
                        Chip(
                            colors = ChipDefaults.chipColors(
                                backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
                            ),
                            onClick = {}
                        ) {
                            Text(
                                text = "$it reps",
                                style = MaterialTheme.typography.caption.copy(
                                    color = MaterialTheme.colors.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    
                    exercise.restSecs?.let {
                        Chip(
                            colors = ChipDefaults.chipColors(
                                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
                            ),
                            onClick = {}
                        ) {
                            Text(
                                text = "${it}s rest",
                                style = MaterialTheme.typography.caption.copy(
                                    color = MaterialTheme.colors.error,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            if (boundDays.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Text(
                        text = "Scheduled for:",
                        style = MaterialTheme.typography.caption.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        boundDays.forEach { day ->
                            Chip(
                                colors = ChipDefaults.chipColors(
                                    backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
                                ),
                                onClick = { onUnbindDay?.invoke(day) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = dayOfWeekLabel(day),
                                        style = MaterialTheme.typography.caption.copy(
                                            color = MaterialTheme.colors.secondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colors.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun dayOfWeekLabel(day: Int): String =
    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").getOrElse(day - 1) { "?" } 