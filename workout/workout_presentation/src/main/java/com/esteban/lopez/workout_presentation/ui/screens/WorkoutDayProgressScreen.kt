package com.esteban.ruano.workout_presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.components.AppBar
import com.esteban.ruano.ui.Blue
import com.esteban.ruano.ui.DarkGray
import com.esteban.ruano.workout_presentation.R
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.models.ExerciseInProgress
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailState

@Composable
fun WorkoutDayProgressScreen(
    onClose: () -> Boolean,
    userIntent: (WorkoutIntent) -> Unit,
    state: WorkoutDayDetailState,
    pagerState: PagerState
) {

    val exercises = state.workout?.exercises ?: emptyList()
    val inProgress = state.exercisesInProgress


    Column {
        AppBar(title = state.time)
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalPager(state = pagerState) { index ->

            ExerciseProgress(
                inProgress = state.exercisesInProgress[index],
                onRepDone = {
                    exercises[index].id?.let {
                        userIntent(
                            WorkoutIntent.DoRep(
                                it
                            )
                        )
                    }
                },
                onRepUndone = {
                    exercises[index].id?.let {
                        userIntent(
                            WorkoutIntent.UndoRep(
                                it
                            )
                        )
                    }
                }
            )
        }
    }


}

@Composable
fun ExerciseProgress(
    inProgress: ExerciseInProgress,
    onRepDone: () -> Unit,
    onRepUndone: () -> Unit
) {
    val done = inProgress.setsDone >= (inProgress.exercise.baseSets?:0)
    val repsDone = inProgress.repsDone
    val setsDone = inProgress.setsDone
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    color = Blue
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.exercising),
                contentDescription = "Exercising"
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = inProgress.exercise.name,
            style = MaterialTheme.typography.h2,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Set ${if (setsDone == inProgress.exercise.baseSets) inProgress.exercise.baseSets else (setsDone + 1)}",
            style = MaterialTheme.typography.subtitle1
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                onRepUndone()
            }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous")
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(150.dp)
            ) {
                CircularProgressIndicator(
                    progress = (repsDone.toFloat() / (inProgress.exercise.baseReps?:0)),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colors.primaryVariant,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "$repsDone / ${inProgress.exercise.baseReps}",
                    style = MaterialTheme.typography.h1.copy(color = DarkGray)
                )
            }
            IconButton(onClick = {
                onRepDone()
            }) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next")
            }
        }
    }
}