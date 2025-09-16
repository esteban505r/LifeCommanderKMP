package com.esteban.ruano.workout_presentation.ui.composable

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.ExerciseSet
import com.esteban.ruano.lifecommander.ui.components.ActionButton
import com.esteban.ruano.lifecommander.ui.components.AddSetRow
import com.esteban.ruano.lifecommander.ui.components.BannerError
import com.esteban.ruano.lifecommander.ui.components.BannerInfo
import com.esteban.ruano.lifecommander.ui.components.SetRow
import com.esteban.ruano.lifecommander.ui.components.StatChip
import com.esteban.ruano.lifecommander.ui.components.StatusChip
import com.esteban.ruano.ui.SoftBlue
import com.esteban.ruano.ui.SoftGreen
import com.esteban.ruano.ui.SoftRed
import com.esteban.ruano.ui.SoftYellow
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.TimeZone
import java.util.UUID


// --------- Dialog variants for completion flow ---------
private sealed class CompletePrompt {
    data class AutoFill(val expectedSets: Int, val expectedReps: Int) : CompletePrompt()
    data class Incomplete(val expectedSets: Int, val doneSets: Int) : CompletePrompt()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExerciseCard(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    sets: List<ExerciseSet>,
    // Actions
    onEdit: (Exercise) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onCompleteExercise: (() -> Unit)? = null,
    onUndoExercise: ((String) -> Unit)? = null,
    onAddSet: (repsDone: Int, exerciseId: String, workoutDayId: String, onResult: (ExerciseSet?) -> Unit) -> Unit,
    onUpdateSetReps: (setId: String, newReps: Int) -> Unit,
    onRemoveSet: (setId: String) -> Unit,
    // Flags
    isCompleted: Boolean = false,
    isAddingSet: Boolean = false,
    showActionButtons: Boolean = true,
    inProgress: Boolean = false,
    defaultReps: Int = 0,
    workoutId: String? = null,
    onShowMoreOptions: (String) -> Unit = {}
) {
    // --- Local UI state ---
    var expanded by remember { mutableStateOf(false) }
    var editableSetId by remember { mutableStateOf<String?>(null) }
    var desiredRepsText by remember { mutableStateOf(defaultReps.coerceAtLeast(0).toString()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showExpectedSetsDialog by remember { mutableStateOf(false) }
    var pendingReps by remember { mutableStateOf<Int?>(null) }

    // Completion prompts & inline error
    var completePrompt by remember { mutableStateOf<CompletePrompt?>(null) }
    var topError by remember { mutableStateOf<String?>(null) }

    // To trigger focus on first set input
    var focusBump by remember { mutableStateOf(0) }
    val keyboard = LocalSoftwareKeyboardController.current

    val canMutateSets = !isCompleted

    // Helpers
    fun startEditingFirstSet() {
        if (sets.isNotEmpty()) {
            expanded = true
            editableSetId = sets.first().id
            focusBump++ // tells SetRow to refocus
        }
    }

    fun addMissingSetsAndComplete(expectedSets: Int, reps: Int) {
        val missing = (expectedSets - sets.size).coerceAtLeast(0)
        if (missing <= 0) {
            onCompleteExercise?.invoke()
            return
        }
        var added = 0
        fun addNext() {
            onAddSet(reps, exercise.id ?: return, workoutId?:"") { result ->
                if (result == null) {
                    errorMsg = "Could not auto-fill sets."
                    return@onAddSet
                }
                added++
                if (added < missing) addNext() else onCompleteExercise?.invoke()
            }
        }
        addNext()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(220)),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(.08f))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {

            // ---------- Header ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isCompleted) MaterialTheme.colors.primary.copy(.18f)
                            else MaterialTheme.colors.primary.copy(.10f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Title + description; leave room for status chip
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 2, // <= limit to 2 lines
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f) // <= reserve space for chip
                        )
                        Spacer(Modifier.width(8.dp))
                        if(inProgress){
                            StatusChip(
                                text = if (isCompleted) "Completed" else "Active",
                                tint = if (isCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.secondary
                            )
                        }
                    }

                    val desc = exercise.description.orEmpty()
                    if (desc.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.body2.copy(
                                color = MaterialTheme.colors.onSurface.copy(.70f)
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ---------- Base stats ----------
            if ((exercise.baseSets ?: 0) > 0 || (exercise.baseReps ?: 0) > 0 || (exercise.restSecs ?: 0) > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if ((exercise.baseSets ?: 0) > 0) StatChip("${exercise.baseSets}", "sets")
                    if ((exercise.baseReps ?: 0) > 0) StatChip("${exercise.baseReps}", "reps")
                    if ((exercise.restSecs ?: 0) > 0) StatChip("${exercise.restSecs}", "rest")
                    Spacer(Modifier.weight(1f))
                    if(inProgress){
                        TextButton(
                            onClick = { expanded = !expanded },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (expanded) "Hide sets" else "Show sets")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (topError != null) {
                BannerError(topError!!)
                Spacer(Modifier.height(12.dp))
            }

            // ---------- Sets ----------
            if(inProgress){
                AnimatedVisibility(visible = expanded) {
                    Column(Modifier.fillMaxWidth()) {
                        if (isCompleted) {
                            Spacer(Modifier.height(12.dp))
                            BannerInfo(
                                text = "Exercise completed — sets are locked.",
                                icon = Icons.Filled.Lock
                            )
                        }


                        Spacer(Modifier.height(12.dp))

                        AddSetRow(
                            enabled = canMutateSets && !isAddingSet,
                            isLoading = isAddingSet,
                            initialReps = defaultReps,
                            onValidate = { reps ->
                                when {
                                    reps <= 0 -> "Reps must be greater than 0."
                                    else -> null
                                }
                            },
                            onAdd = { reps ->
                                val expected = exercise.baseSets ?: 0
                                if (expected > 0 && sets.size >= expected) {
                                    pendingReps = reps
                                    showExpectedSetsDialog = true
                                    return@AddSetRow
                                }

                                errorMsg = null
                                onAddSet(
                                    reps,
                                    exercise.id ?: return@AddSetRow,
                                    workoutId ?: ""
                                ) { newSet ->
                                    if (newSet != null) {
                                        editableSetId = newSet.id     // immediately jump into edit mode for the new set
                                        // leave AddSetRow’s field at default again
                                    } else {
                                        errorMsg = "Could not add set. Try again."
                                    }
                                }
                            }
                        )

                        Spacer(Modifier.height(12.dp))

                        // List (caps height if many sets)
                        val listMaxHeight = 220.dp
                        val useLazy = sets.size > 5
                        Log.d("Sets",sets.toString())
                        if (useLazy) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = listMaxHeight)
                            ) {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(sets.size, key = { sets[it].id }) { index ->
                                        val set = sets[index]
                                        val index = sets.indexOf(set)
                                        SetRow(
                                            index = index,
                                            set = set,
                                            editableSetId = editableSetId,
                                            canMutate = canMutateSets,
                                            onEditToggle = {
                                                editableSetId =
                                                    if (editableSetId == set.id) null else set.id
                                            },
                                            onSave = { newReps ->
                                                onUpdateSetReps(set.id, newReps)
                                                editableSetId = null
                                            },
                                            onRemove = { onRemoveSet(set.id) },
                                            focusBump = focusBump
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                sets.forEachIndexed { index, set ->
                                    SetRow(
                                        index = index,
                                        set = set,
                                        editableSetId = editableSetId,
                                        canMutate = canMutateSets,
                                        onEditToggle = {
                                            editableSetId =
                                                if (editableSetId == set.id) null else set.id
                                        },
                                        onSave = { newReps ->
                                            onUpdateSetReps(set.id, newReps)
                                            editableSetId = null
                                        },
                                        onRemove = { onRemoveSet(set.id) },
                                        focusBump = focusBump
                                    )
                                }
                            }
                        }

                        if (errorMsg != null) {
                            Spacer(Modifier.height(8.dp))
                            BannerError(errorMsg!!)
                        }

                        // Add set row
                        Spacer(Modifier.height(12.dp))


                        if (errorMsg != null) {
                            Spacer(Modifier.height(8.dp))
                            BannerError(errorMsg!!)
                        }

                        Spacer(Modifier.height(12.dp))





                    }
                }
            }
            // ---------- Bottom actions ----------
            if (showActionButtons) {
                Spacer(Modifier.height(16.dp))

                Divider(color = MaterialTheme.colors.onSurface.copy(.08f), thickness = 1.dp)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        if (onCompleteExercise != null && inProgress) {
                            ActionButton(
                                onClick = {
                                    if(!isCompleted){
                                        val expectedSets = exercise.baseSets ?: 0
                                        val expectedReps =
                                            (exercise.baseReps ?: defaultReps).coerceAtLeast(0)

                                        // Case A: no sets at all -> block with error
                                        if (sets.isEmpty()) {
                                            topError = "Add at least one set before completing."
                                            return@ActionButton
                                        }

                                        // Case B: sets hidden & not all expected done -> propose auto-fill or edit
                                        if (!expanded && expectedSets > 0 && sets.size < expectedSets) {
                                            completePrompt =
                                                CompletePrompt.AutoFill(expectedSets, expectedReps)
                                            return@ActionButton
                                        }

                                        // Case C: partially done (some < expected) -> warn
                                        if (expectedSets > 0 && sets.size in 1 until expectedSets) {
                                            completePrompt =
                                                CompletePrompt.Incomplete(expectedSets, sets.size)
                                            return@ActionButton
                                        }

                                        // Case D: good to complete
                                        onCompleteExercise()
                                    }
                                    else{
                                        onUndoExercise?.invoke(
                                            exercise.id?:""
                                        )
                                    }
                                },
                                icon = if (!isCompleted) Icons.Filled.CheckCircle else Icons.AutoMirrored.Filled.Undo,
                                label = if (!isCompleted) "Completed" else "Undo",
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if(!isCompleted ) SoftGreen else SoftYellow,
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp,
                                    if(!isCompleted)
                                    SoftGreen.copy(.25f)
                                    else SoftYellow.copy(.25f)
                                )
                            )
                        }

                        ActionButton(
                            onClick = { onEdit(exercise) },
                            icon = Icons.Outlined.Edit,
                            label = "Edit",
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = MaterialTheme.colors.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(.15f))
                        )

                        ActionButton(
                            onClick = { exercise.id?.let { onShowMoreOptions.invoke(it) } },
                            icon = Icons.Outlined.Menu,
                            label = "Show menu",
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = SoftBlue,
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, SoftRed.copy(.25f))
                        )
                    }
                }
            }
        }
    }

    // ---------- Dialog: expected sets reached (manual add path) ----------
    if (showExpectedSetsDialog) {
        AlertDialog(
            onDismissRequest = { showExpectedSetsDialog = false; pendingReps = null },
            title = { Text("Expected sets reached") },
            text = { Text("You’ve already completed ${exercise.baseSets} set(s) for ${exercise.name}. Add another?") },
            confirmButton = {
                TextButton(onClick = {
                    val reps = pendingReps ?: return@TextButton
                    showExpectedSetsDialog = false
                    pendingReps = null
                    errorMsg = null
                    onAddSet(reps, exercise.id ?: return@TextButton, workoutId?:"") { newSet ->
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

    // ---------- Dialogs for completion flow ----------
    when (val p = completePrompt) {
        is CompletePrompt.AutoFill -> {
            AlertDialog(
                onDismissRequest = { completePrompt = null },
                title = { Text("Complete all sets?") },
                text = {
                    Text("You’ve done ${sets.size} of ${p.expectedSets} sets. Fill the remaining sets with ${p.expectedReps} reps each, or edit them first?")
                },
                confirmButton = {
                    TextButton(onClick = {
                        completePrompt = null
                        addMissingSetsAndComplete(p.expectedSets, p.expectedReps)
                    }) { Text("Fill & complete") }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        completePrompt = null
                        startEditingFirstSet()
                    }) { Text("Edit sets") }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
        is CompletePrompt.Incomplete -> {
            AlertDialog(
                onDismissRequest = { completePrompt = null },
                title = { Text("Sets incomplete") },
                text = {
                    Text("You’ve recorded ${p.doneSets} of ${p.expectedSets} sets. Do you want to complete anyway, or finish the sets?")
                },
                confirmButton = {
                    TextButton(onClick = {
                        completePrompt = null
                        onCompleteExercise?.invoke()
                    }) { Text("Complete anyway") }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        completePrompt = null
                        startEditingFirstSet()
                    }) { Text("Finish sets") }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
        null -> Unit
    }
}


private fun sampleExercise(
    name: String = "Plank Series",
    desc: String = "Core stability & endurance.",
    sets: Int? = 3,
    reps: Int? = 45,
    rest: Int? = 30,
    id: String = "ex-${UUID.randomUUID()}"
) = Exercise(
    id = id,
    name = name,
    description = desc,
    baseSets = sets,
    baseReps = reps,
    restSecs = rest
)

private fun sampleSet(reps: Int) = ExerciseSet(
    id = "set-${UUID.randomUUID()}",
    reps = reps,
    doneDateTime = getCurrentDateTime(
        TimeZone.currentSystemDefault()
    ).formatDefault()
)

/* ---------- Previews ---------- */

@Preview(name = "ExerciseCard • Active", showBackground = true, widthDp = 360)
@Composable
fun Preview_ExerciseCard_Active() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            color = MaterialTheme.colors.background
        ) {
            val exercise = sampleExercise()
            val sets = listOf(sampleSet(15), sampleSet(15)) // 2/3 sets done

            ExerciseCard(
                exercise = exercise,
                sets = sets,
                onEdit = { /* no-op preview */ },
                // Callbacks (no-ops / simple stubs for preview)
                onDelete = { /* no-op preview */ },
                onCompleteExercise = { /* no-op preview */ },
                onAddSet = { repsDone, _, _, onResult ->
                    // Pretend we created a set successfully
                    onResult(sampleSet(repsDone))
                },
                onUpdateSetReps = { _, _ -> },
                onRemoveSet = { /* no-op */ },
                defaultReps = 12,
            )
        }
    }
}

@Preview(name = "ExerciseCard • Completed", showBackground = true, widthDp = 360)
@Composable
fun Preview_ExerciseCard_Completed() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            color = MaterialTheme.colors.background
        ) {
            val exercise = sampleExercise(name = "Push Ups", sets = 4, reps = 12, rest = 45)
            val sets = List(4) { sampleSet(12) }

            ExerciseCard(
                exercise = exercise,
                sets = sets,
                onCompleteExercise = {},
                onAddSet = { _, _, _, onResult -> onResult(null) },
                onUpdateSetReps = { _, _ -> },
                onRemoveSet = { }, // locked in completed state
                isCompleted = true,
                defaultReps = 12,
            )
        }
    }
}

@Preview(name = "ExerciseCard • Long list (scroll)", showBackground = true, widthDp = 360)
@Composable
fun Preview_ExerciseCard_LongList() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            color = MaterialTheme.colors.background
        ) {
            val exercise = sampleExercise(name = "Dumbbell Rows", sets = 8, reps = 10, rest = 60)
            val sets = List(7) { sampleSet(10) } // triggers LazyColumn in your card

            ExerciseCard(
                exercise = exercise,
                sets = sets,
                onCompleteExercise = {},
                onAddSet = { repsDone, _, _, onResult -> onResult(sampleSet(repsDone)) },
                onUpdateSetReps = { _, _ -> },
                onRemoveSet = { },
                defaultReps = 10,
            )
        }
    }
}