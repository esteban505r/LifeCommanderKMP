package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.ExerciseSet
import com.esteban.ruano.lifecommander.ui.state.WorkoutState
import ui.composables.NewEditExerciseDialog

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WorkoutScreen(
    state: WorkoutState,
    onAdd: (Exercise) -> Unit,
    onUpdate: (Exercise) -> Unit,
    onDelete: (String) -> Unit,
    onDaySelected: (Int) -> Unit,
    onCompleteWorkout: (Int) -> Unit = {},
    onCompleteExercise: (String,Int, String) -> Unit = { _,_, _ -> },
    onUncompleteExercise: (String) -> Unit = { _ -> },
    onChangeAllExercisesMode: (Boolean) -> Unit = {},
    onGetAllExercises: () -> Unit = {},
    onBindExerciseToDay: (String, Int, (Boolean) -> Unit) -> Unit = { _, _, _ -> },
    onUnbindExerciseFromDay: (String, Int, (Boolean) -> Unit) -> Unit = { _, _, _ -> },
    onAddSet: (repsDone: Int, exerciseId:String, workoutDayId:String, onResult: (ExerciseSet?) -> Unit) -> Unit,
    onUpdateSetReps: (setId: String, newReps: Int) -> Unit,
    onRemoveSet: (setId: String) -> Unit,
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
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

                    if (
                        !allExercisesMode
                    ) {
                        Button(
                            onClick = {
                                onCompleteWorkout(
                                    state.daySelected
                                )
                            },
                            enabled = !state.isCompleted,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = MaterialTheme.colors.onPrimary
                            ),
                            elevation = ButtonDefaults.elevation(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Done,
                                contentDescription = "Add Exercise",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Complete workout",
                                style = MaterialTheme.typography.button.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
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
                        workoutDayId = state.workoutDays.firstOrNull()?.id!!,
                        onUpdateExercise = {
                            exerciseToEdit = it
                            showExerciseDialog = true
                        },
                        onDeleteExercise = onDelete,
                        boundDays = boundDays.toList(),
                        onUnbindDay = { day ->
                            onUnbindExerciseFromDay(exercise.id, day) { success ->
                                if (success) onGetAllExercises()
                            }
                        },
                        onBindToDay = if (allExercisesMode) {
                            { showBindDialog = exercise }
                        } else null,
                        onUncompleteExercise = if (!allExercisesMode && state.workoutDays.isNotEmpty()) {
                            {
                                val workoutDayId = state.workoutDays.firstOrNull()?.id
                                if (workoutDayId != null) {
                                    onUncompleteExercise(state.completedExercises.firstOrNull{it.exerciseId == exercise.id}?.exerciseTrackId?:"")
                                }
                            }
                        } else null,
                        onCompleteExercise = if (!allExercisesMode && state.workoutDays.isNotEmpty()) {
                            {
                                val workoutDayId = state.workoutDays.firstOrNull()?.id
                                if (workoutDayId != null) {
                                    onCompleteExercise(exercise.id, state.daySelected,workoutDayId)
                                }
                            }
                        } else null,
                        isAllExercisesMode = allExercisesMode,
                        isCompleted = state.completedExercises.firstOrNull {
                            it.exerciseId == exercise.id && it.exerciseDone
                        } != null,
                        onAddSet = onAddSet,
                        sets = state.completedExercises.firstOrNull{
                            it.exerciseId == exercise.id
                        }?.setsDone?:listOf(),
                        onRemoveSet = onRemoveSet,
                        onUpdateSetReps = onUpdateSetReps,
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

/**
 * ExerciseCard — backend-driven sets:
 * - DOES NOT add sets locally on "Add Set" click.
 * - Calls `onAddSet(desiredReps) { result }` and waits for the parent/backend to confirm.
 * - Only the just-created set (from backend) becomes editable (reps); all others are read-only.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExerciseCard(
    exercise: Exercise,
    sets: List<ExerciseSet>,
    boundDays: List<Int> = emptyList(),
    workoutDayId:String,
    // High-level actions
    onUpdateExercise: (Exercise) -> Unit,
    onDeleteExercise: (String) -> Unit,
    onCompleteExercise: (() -> Unit)? = null,
    onUncompleteExercise: (() -> Unit)? = null,
    onBindToDay:((Int) -> Unit)? = null,
    onUnbindDay: ((Int) -> Unit)? = null,

    // Set-specific actions (backend-driven)
    onAddSet: (repsDone: Int, exerciseId:String, workoutDayId:String, onResult: (ExerciseSet?) -> Unit) -> Unit,
    onUpdateSetReps: (setId: String, newReps: Int) -> Unit,
    onRemoveSet: (setId: String) -> Unit,

    // UI flags
    isCompleted: Boolean = false,
    isAllExercisesMode: Boolean = false, // reserved for future layout tweaks
) {
    // Which set (if any) is currently allowed to edit reps
    var editableSetId by remember { mutableStateOf<String?>(null) }
    // Text for the "next" set request
    var desiredRepsText by remember { mutableStateOf(exercise.baseReps.toString()) }
    // Loading state while waiting backend confirmation for Add
    var isAdding by remember { mutableStateOf(false) }
    // Optional feedback state
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var showExpectedSetsDialog by remember { mutableStateOf(false) }
    var pendingRepsToAdd by remember { mutableStateOf<Int?>(null) }
    val canMutateSets = !isCompleted


    if (showExpectedSetsDialog) {
        AlertDialog(
            onDismissRequest = {
                showExpectedSetsDialog = false
                pendingRepsToAdd = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colors.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Expected sets reached")
                }
            },
            text = {
                val expected = exercise.baseSets ?: 0
                Text(
                    "You’ve already completed $expected set${if (expected == 1) "" else "s"} for ${exercise.name}.\n" +
                            "Do you still want to add another set?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val reps = pendingRepsToAdd
                        if (reps != null) {
                            showExpectedSetsDialog = false
                            pendingRepsToAdd = null
                            // Proceed with the same backend-driven add flow
                            isAdding = true
                            errorMsg = null
                            onAddSet(reps, exercise.id, workoutDayId) { newSetOrNull ->
                                isAdding = false
                                if (newSetOrNull != null) {
                                    editableSetId = newSetOrNull.id
                                    desiredRepsText = exercise.baseReps.toString()
                                } else {
                                    errorMsg = "Could not add set. Try again."
                                }
                            }
                        } else {
                            showExpectedSetsDialog = false
                        }
                    }
                ) { Text("Add anyway") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExpectedSetsDialog = false
                        pendingRepsToAdd = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancel") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

            // ===== Top Row: Title + Scheduled Days =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold)
                    )
                    exercise.description?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.body2.copy(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (boundDays.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    FlowRow {
                        boundDays.forEach { day ->
                            Chip(
                                onClick = { onUnbindDay?.invoke(day) },
                                colors = ChipDefaults.chipColors(
                                    backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.15f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        dayOfWeekLabel(day),
                                        color = MaterialTheme.colors.secondary,
                                        style = MaterialTheme.typography.caption
                                    )
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colors.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ===== Base info chips =====
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                exercise.baseSets?.let {
                    Chip(
                        onClick = {},
                        colors = ChipDefaults.chipColors(
                            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.15f)
                        )
                    ) { Text("$it sets", color = MaterialTheme.colors.primary) }
                }
                exercise.baseReps?.let {
                    Chip(
                        onClick = {},
                        colors = ChipDefaults.chipColors(
                            backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.15f)
                        )
                    ) { Text("$it reps", color = MaterialTheme.colors.secondary) }
                }
                exercise.restSecs?.let {
                    Chip(
                        onClick = {},
                        colors = ChipDefaults.chipColors(
                            backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.15f)
                        )
                    ) { Text("${it}s rest", color = MaterialTheme.colors.error) }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ===== Sets List (read-only, except the most recently created set) =====
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Sets",
                    style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(Modifier.height(8.dp))

                if (sets.isEmpty()) {
                    Text(
                        "No sets yet. Add your first one below.",
                        style = MaterialTheme.typography.body2.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    )
                } else {
                    if (isCompleted) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null,
                                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Exercise completed — sets are locked.",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                        items(sets.size, key = { sets[it].id }) { setIndex ->
                            val set = sets[setIndex]
                            var editText by remember(set.id) { mutableStateOf(set.reps.toString()) }
                            val isEditable = editableSetId == set.id

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        if (isEditable)
                                            MaterialTheme.colors.primary.copy(alpha = 0.08f)
                                        else
                                            MaterialTheme.colors.onSurface.copy(alpha = 0.035f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left: Set label + reps (editable ONLY if this is the last created one)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Set ",
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (isEditable) {
                                        TextField(
                                            value = editText,
                                            onValueChange = { v ->
                                                // digits only
                                                editText = v.filter { it.isDigit() }.take(4)
                                            },
                                            singleLine = true,
                                            modifier = Modifier.width(80.dp),
                                            textStyle = TextStyle(fontSize = 14.sp),
                                            colors = TextFieldDefaults.textFieldColors(
                                                backgroundColor = Color.Transparent,
                                                focusedIndicatorColor = MaterialTheme.colors.primary,
                                                unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                                textColor = MaterialTheme.colors.onSurface
                                            ),
                                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                        )
                                        Text(" reps", fontSize = 14.sp)
                                    } else {
                                        Text("${set.reps} reps", fontSize = 14.sp)
                                    }
                                }

                                // Right: actions per item
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Save only appears while editable
                                    if (isEditable) {
                                        IconButton(
                                            onClick = {
                                                val parsed = editText.toIntOrNull()
                                                if (parsed != null) {
                                                    onUpdateSetReps(set.id, parsed)
                                                    editableSetId = null
                                                    errorMsg = null
                                                } else {
                                                    errorMsg = "Enter a valid number of reps."
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Save reps")
                                        }
                                    }

                                    IconButton(onClick = { onRemoveSet(set.id) }, enabled = canMutateSets) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove set",
                                            tint = MaterialTheme.colors.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Optional inline error
                if (errorMsg != null) {
                    Text(
                        errorMsg!!,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption
                    )
                    Spacer(Modifier.height(6.dp))
                }

                // ===== Add set row (doesn't mutate local state; calls backend) =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = desiredRepsText,
                        onValueChange = { desiredRepsText = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Reps") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 14.sp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.05f),
                            focusedIndicatorColor = MaterialTheme.colors.primary,
                            unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        enabled = !isAdding
                    )

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val reps = desiredRepsText.toIntOrNull()
                            if (reps == null) {
                                errorMsg = "Enter a valid number of reps."
                                return@Button
                            }

                            val expectedSets = exercise.baseSets
                            if (expectedSets != null && sets.size >= expectedSets) {
                                pendingRepsToAdd = reps
                                showExpectedSetsDialog = true
                                return@Button
                            }

                            isAdding = true
                            errorMsg = null
                            // Ask parent to add; when backend confirms, parent calls onResult with the new set
                            onAddSet(reps,exercise.id, workoutDayId) { newSetOrNull ->
                                isAdding = false
                                if (newSetOrNull != null) {
                                    // Allow editing ONLY for the fresh set
                                    editableSetId = newSetOrNull.id
                                    // keep desired reps ready for the next quick add
                                    desiredRepsText = exercise.baseReps.toString()
                                } else {
                                    errorMsg = "Could not add set. Try again."
                                }
                            }
                        },
                        enabled = !isAdding && canMutateSets,
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                        modifier = Modifier.height(56.dp)
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text("Add Set", color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ===== Bottom Row: Action buttons (Edit / Delete / Complete-or-Undo) =====
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { onUpdateExercise(exercise) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Edit")
                }

                OutlinedButton(
                    onClick = { onDeleteExercise(exercise.id) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete")
                }

                if (onCompleteExercise != null) {
                    val buttonColors = if (!isCompleted)
                        ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    else
                        ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)

                    Button(
                        onClick = (if(!isCompleted) onCompleteExercise else onUncompleteExercise)?:{},
                        modifier = Modifier.weight(1f),
                        colors = buttonColors
                    ) {
                        Icon(
                            if (!isCompleted) Icons.Default.CheckCircle else Icons.Default.Undo,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (!isCompleted) "Complete" else "Undo", color = Color.White)
                    }
                }
            }
        }
    }
}
fun dayOfWeekLabel(day: Int): String =
    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").getOrElse(day - 1) { "?" } 