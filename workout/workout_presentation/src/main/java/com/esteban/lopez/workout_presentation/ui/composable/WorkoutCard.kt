package com.esteban.ruano.workout_presentation.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.theme.Gray
import com.esteban.ruano.core_ui.theme.Gray3
import com.esteban.ruano.lifecommander.models.Exercise

@Composable
fun WorkoutCard(modifier: Modifier = Modifier, exercises: List<Exercise>) {
    Column(
        modifier = modifier
    ) {
        for(exercise in exercises.take(4)) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.subtitle1.copy(color = Gray3),
                    modifier = Modifier.padding(end = 16.dp).weight(2f)
                )
                Text(
                    text = "${exercise.baseSets} sets of ${exercise.baseReps} reps",
                    style = MaterialTheme.typography.subtitle2.copy(color = Gray),
                    modifier = Modifier.padding(horizontal = 4.dp).weight(1f)
                )
            }
        }
    }
}