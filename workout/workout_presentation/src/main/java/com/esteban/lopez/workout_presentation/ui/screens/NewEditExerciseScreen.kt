package com.esteban.ruano.workout_presentation.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core.utils.UiText
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.utils.CustomSnackbarVisualsWithUiText
import com.esteban.ruano.core_ui.utils.SnackbarController
import com.esteban.ruano.core_ui.utils.SnackbarEvent
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.ui.SoftGreen
import com.esteban.ruano.ui.SoftRed
import com.esteban.ruano.workout_domain.model.MuscleGroup
import com.esteban.ruano.workout_domain.model.toMuscleGroupString
import com.esteban.ruano.workout_presentation.intent.ExercisesIntent
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.ExercisesState
import com.esteban.ruano.workout_presentation.utils.toResourceString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewEditExerciseScreen(
    exerciseToEditId: String? = null,
    state: ExercisesState,
    onDone:()->Unit,
    userIntent: (ExercisesIntent) -> Unit,
) {
    val editMode = exerciseToEditId != null
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // ===== Form fields (saveable) =====
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var restSecs by rememberSaveable { mutableStateOf("") }
    var baseSets by rememberSaveable { mutableStateOf("") }
    var baseReps by rememberSaveable { mutableStateOf("") }
    var muscleGroup by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var mgExpanded by remember { mutableStateOf(false) }

    // Hydrate-once flag so VM updates don’t clobber user edits
    var hydrated by rememberSaveable { mutableStateOf(false) }

    // Request load if we're editing and the item isn't in state yet
    LaunchedEffect(editMode, exerciseToEditId) {
        if (editMode && state.editingExercise == null) {
            userIntent(ExercisesIntent.FetchExercise(exerciseToEditId))
        }
    }

    // Hydrate fields WHEN the VM surfaces editingExercise, only once
    LaunchedEffect(state.editingExercise) {
        val ex = state.editingExercise
        if (editMode && ex != null && !hydrated) {
            name = ex.name.orEmpty()
            description = ex.description.orEmpty()
            restSecs = ex.restSecs?.toString().orEmpty()
            baseSets = ex.baseSets?.toString().orEmpty()
            baseReps = ex.baseReps?.toString().orEmpty()
            muscleGroup = ex.muscleGroup.orEmpty()
            hydrated = true
        }
    }

    // Bubble error messages through your snackbar system
    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage ?: return@LaunchedEffect
        scope.launch {
            SnackbarController.sendEvent(
                SnackbarEvent(
                    CustomSnackbarVisualsWithUiText.fromType(
                        SnackbarType.ERROR,
                        UiText.DynamicString(msg)
                    )
                )
            )
        }
    }

    fun validate(): Boolean {
        val nonEmpty =
            name.isNotBlank() &&
                    description.isNotBlank() &&
                    restSecs.isNotBlank() &&
                    baseSets.isNotBlank() &&
                    baseReps.isNotBlank() &&
                    muscleGroup.isNotBlank()

        val numericOk =
            restSecs.toIntOrNull() != null &&
                    baseSets.toIntOrNull() != null &&
                    baseReps.toIntOrNull() != null

        return nonEmpty && numericOk
    }

    fun showError(msg: UiText) = scope.launch {
        SnackbarController.sendEvent(
            SnackbarEvent(CustomSnackbarVisualsWithUiText.fromType(SnackbarType.ERROR, msg))
        )
    }

    fun onSubmit() {
        val ok = validate()
        if (!ok) {
            showErrors = true
            val msg =
                if (name.isBlank() || description.isBlank() || restSecs.isBlank()
                    || baseSets.isBlank() || baseReps.isBlank() || muscleGroup.isBlank()
                ) UiText.StringResource(R.string.error_empty_fields)
                else UiText.StringResource(R.string.error_invalid_fields)
            showError(msg)
            return
        }

        val model = Exercise(
            name = name.trim(),
            description = description.trim(),
            restSecs = restSecs.toInt(),
            baseSets = baseSets.toInt(),
            baseReps = baseReps.toInt(),
            muscleGroup = muscleGroup
        )

        scope.launch {
            if (editMode) {
                userIntent(ExercisesIntent.UpdateExercise(exerciseToEditId, model){
                    onDone()
                })
            } else {
                userIntent(ExercisesIntent.SaveExercise(model){
                    onDone()
                })
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editMode) "Edit Exercise" else "New Exercise",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { userIntent(ExercisesIntent.NavigateUp) } }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (editMode) {
                        IconButton(
                            onClick = {
//                                scope.launch { userIntent(ExercisesIntent.DeleteExercise(exerciseToEditId)) }
                            }
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = SoftRed)
                        }
                    }
                    IconButton(onClick = { onSubmit() }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save", tint = SoftGreen)
                    }
                },
                elevation = 0.dp,
                backgroundColor = MaterialTheme.colors.surface
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            // ===== Content =====
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .animateContentSize()
            ) {
                Spacer(Modifier.height(12.dp))

                // — Identity —
                Card(elevation = 0.dp, backgroundColor = MaterialTheme.colors.surface) {
                    Column(Modifier.padding(16.dp)) {
                        LabeledField(
                            label = "Name",
                            value = name,
                            onValue = { name = it },
                            placeholder = "e.g., Bench Press",
                            leading = { Icon(Icons.Outlined.FitnessCenter, null) },
                            error = showErrors && name.isBlank(),
                            ime = ImeAction.Next
                        )
                        LabeledField(
                            label = "Description",
                            value = description,
                            onValue = { description = it },
                            placeholder = "Short purpose or cues",
                            leading = { Icon(Icons.Outlined.Info, null) },
                            singleLine = false,
                            error = showErrors && description.isBlank(),
                            ime = ImeAction.Next
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Divider()
                Spacer(Modifier.height(10.dp))

                // — Prescription —
                Card(elevation = 0.dp, backgroundColor = MaterialTheme.colors.surface) {
                    Column(Modifier.padding(16.dp)) {
                        NumberField(
                            label = "Rest (seconds)",
                            value = restSecs,
                            onValue = { restSecs = it.filter(Char::isDigit).take(4) },
                            placeholder = "e.g., 60",
                            leading = { Icon(Icons.Filled.Schedule, null) },
                            error = showErrors && restSecs.toIntOrNull() == null,
                            ime = ImeAction.Next
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NumberField(
                                modifier = Modifier.weight(1f),
                                label = "Base Sets",
                                value = baseSets,
                                onValue = { baseSets = it.filter(Char::isDigit).take(3) },
                                placeholder = "e.g., 4",
                                leading = { Icon(Icons.Filled.ViewList, null) },
                                error = showErrors && baseSets.toIntOrNull() == null,
                                ime = ImeAction.Next
                            )
                            NumberField(
                                modifier = Modifier.weight(1f),
                                label = "Base Reps",
                                value = baseReps,
                                onValue = { baseReps = it.filter(Char::isDigit).take(3) },
                                placeholder = "e.g., 12",
                                leading = { Icon(Icons.Filled.ViewList, null) },
                                error = showErrors && baseReps.toIntOrNull() == null,
                                ime = ImeAction.Done,
                                onImeDone = { onSubmit() }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Divider()
                Spacer(Modifier.height(10.dp))

                // — Muscle Group —
                MuscleGroupPicker(
                    muscleGroup,
                    onChange = {
                        muscleGroup = it
                    },
                    showErrors
                )
                Spacer(Modifier.height(96.dp)) // space for bottom bar
            }

            // ===== Loading overlay =====
            if (state.loading) {
                Surface(
                    color = MaterialTheme.colors.surface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/* ---------- Reusable fields (same as earlier answer) ---------- */

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    placeholder: String,
    leading: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    error: Boolean = false,
    ime: ImeAction = ImeAction.Next,
    onImeDone: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = leading,
        singleLine = singleLine,
        isError = error,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            imeAction = ime,
            keyboardType = if (singleLine) KeyboardType.Text else KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(onDone = { onImeDone?.invoke() })
    )
    if (error) {
        Spacer(Modifier.height(6.dp))
        Text(
            "$label is required",
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.caption
        )
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    placeholder: String,
    leading: @Composable (() -> Unit)? = null,
    error: Boolean = false,
    ime: ImeAction = ImeAction.Next,
    onImeDone: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = leading,
        singleLine = true,
        isError = error,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ime),
        keyboardActions = KeyboardActions(onDone = { onImeDone?.invoke() })
    )
    if (error) {
        Spacer(Modifier.height(6.dp))
        Text(
            "$label must be a number",
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.caption
        )
    }
    Spacer(Modifier.height(12.dp))
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MuscleGroupPicker(
    muscleGroup: String,
    onChange: (String) -> Unit,
    showErrors: Boolean
) {
    var mgExpanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp)) {
        Text(
            text = "Muscle Group",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = mgExpanded,
            onExpandedChange = { mgExpanded = !mgExpanded }
        ) {
            OutlinedTextField(
                value = MuscleGroup.fromValue(muscleGroup).toResourceString(LocalContext.current),
                onValueChange = {},                // readOnly field
                readOnly = true,
                label = { Text("Select group") },
                leadingIcon = { Icon(Icons.Outlined.Tag, null) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = mgExpanded)
                },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && muscleGroup.isBlank(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            // ✅ The menu goes here (not another Box)
            ExposedDropdownMenu(
                expanded = mgExpanded,
                onDismissRequest = { mgExpanded = false }
            ) {
                MuscleGroup.entries.forEach { mg ->
                    DropdownMenuItem(onClick = {
                        onChange(mg.toMuscleGroupString())
                        mgExpanded = false
                    }) {
                        Text(mg.toResourceString(LocalContext.current))
                    }
                }
            }
        }

        if (showErrors && muscleGroup.isBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Please select a muscle group",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }
    }
}

