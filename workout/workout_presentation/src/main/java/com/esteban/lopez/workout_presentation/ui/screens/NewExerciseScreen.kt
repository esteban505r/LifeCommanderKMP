package com.esteban.ruano.workout_presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core.utils.UiText
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.composables.GeneralOutlinedTextField
import com.esteban.ruano.core_ui.utils.CustomSnackBarVisuals
import com.esteban.ruano.core_ui.utils.CustomSnackbarVisualsWithUiText
import com.esteban.ruano.core_ui.utils.SnackbarController
import com.esteban.ruano.core_ui.utils.SnackbarEvent
import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_domain.model.MuscleGroup
import com.esteban.ruano.workout_domain.model.toMuscleGroupString
import com.esteban.ruano.workout_presentation.intent.ExercisesIntent
import com.esteban.ruano.workout_presentation.utils.toResourceString
import kotlinx.coroutines.launch

@Composable
fun NewExerciseScreen(
    userIntent: (ExercisesIntent) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var restSecs by remember { mutableStateOf("") }
    var baseSets by remember { mutableStateOf("") }
    var baseReps by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf("") }
    var dropDownMenuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.padding(top = 16.dp))
            AppBar(
                title = "New Exercise",
                onClose = {
                    coroutineScope.launch {
                        userIntent(ExercisesIntent.NavigateUp)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                if(name.isEmpty() || description.isEmpty() || restSecs.isEmpty() || baseSets.isEmpty() || baseReps.isEmpty() || muscleGroup.isEmpty()) {
                                    SnackbarController.sendEvent(
                                        SnackbarEvent(
                                            CustomSnackbarVisualsWithUiText.fromType(
                                                SnackbarType.ERROR,
                                                UiText.StringResource(R.string.error_empty_fields)
                                            )
                                        )
                                    )
                                    return@launch
                                }
                                if(restSecs.toIntOrNull() == null || baseSets.toIntOrNull() == null || baseReps.toIntOrNull() == null) {
                                    SnackbarController.sendEvent(
                                        SnackbarEvent(
                                            CustomSnackbarVisualsWithUiText.fromType(
                                                SnackbarType.ERROR,
                                                UiText.StringResource(R.string.error_invalid_fields)
                                            )
                                        )
                                    )
                                    return@launch
                                }
                                userIntent(
                                    ExercisesIntent.SaveExercise(
                                        Exercise(
                                            name = name,
                                            description = description,
                                            restSecs = restSecs.toInt(),
                                            baseSets = baseSets.toInt(),
                                            baseReps = baseReps.toInt(),
                                            muscleGroup = muscleGroup,
                                        )
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
            GeneralOutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                }, placeHolder = "Name"
            )
            GeneralOutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                placeHolder = "Description"
            )
            GeneralOutlinedTextField(
                value = restSecs,
                onValueChange = {
                    restSecs = it
                },
                placeHolder = "Rest Seconds"
            )
            GeneralOutlinedTextField(
                value = baseSets,
                onValueChange = {
                    baseSets = it
                },
                placeHolder = "Base Sets"
            )
            GeneralOutlinedTextField(
                value = baseReps,
                onValueChange = {
                    baseReps = it
                },
                placeHolder = "Base Reps"
            )
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Muscle Group: ${MuscleGroup.fromValue(muscleGroup).toResourceString(context)}")
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    IconButton(
                        onClick = {
                            dropDownMenuExpanded = true
                        }
                    ) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Drop Down")
                    }
                }
                DropdownMenu(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(0.dp),
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = {
                        dropDownMenuExpanded = false
                    },
                ) {
                    MuscleGroup.entries.forEach {
                        TextButton(onClick = {
                            muscleGroup = it.toMuscleGroupString()
                            dropDownMenuExpanded = false
                        }) {
                            Text(it.toResourceString(
                                context
                            ))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 16.dp))
        }
    }
}