package com.esteban.ruano.workout_presentation.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.RoundedCornerCheckbox
import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.MuscleGroup
import com.esteban.ruano.workout_presentation.utils.toResourceString

@Composable
fun ExerciseCard(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    checked: Boolean? = null,
    onExerciseClick: ((String?) -> Unit)? = null,
    onSelectExercise: ((Exercise,Boolean) -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onExerciseClick?.invoke(exercise.id)
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (checked != null) {
                RoundedCornerCheckbox (
                    isChecked = checked,
                    onValueChange = {
                        onSelectExercise?.invoke(exercise,it)
                    },
                    size = 32f,
                    checkedColor = MaterialTheme.colors.secondary,
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
            Box(
                modifier = Modifier.size(50.dp)
            ) {
                exercise.resource?.let {
                    AsyncImage(
                        model = exercise.resource?.url,
                        contentDescription = "Exercise Image",
                        modifier = Modifier
                            .background(
                                color = Color.Gray
                            )
                            .fillMaxHeight()
                    )
                } ?: Image(
                    painter = painterResource(R.drawable.workout_girl),
                    contentDescription = "Workout image"
                )
            }
            Text(exercise.name, style = MaterialTheme.typography.subtitle1)
        }
        Column {
            Text(
                text = exercise.muscleGroup.toResourceString(LocalContext.current),
                style = MaterialTheme.typography.subtitle2
            )
            Text(
                text = "${exercise.baseSets} sets x ${exercise.baseReps} reps",
                style = MaterialTheme.typography.subtitle2
            )
            Text(
                text = "${exercise.restSecs} seconds rest",
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}

@Preview
@Composable
fun ExerciseCardPreview() {
    Box(
        modifier = Modifier.background(
            color = Color.White
        )
    ) {
        ExerciseCard(
            exercise = Exercise(
                name = "Bench Press",
                description = "Lay down on a bench and press the bar up",
                restSecs = 60,
                baseSets = 3,
                baseReps = 10,
                muscleGroup = MuscleGroup.CORE,
                equipment = emptyList()
            )
        )
    }
}
