package com.esteban.ruano.workout_presentation.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.composables.ListTile
import com.esteban.ruano.core_ui.utils.DateUIUtils.toDayOfTheWeekString
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WorkoutDayDetailScreen(
    workoutId: String,
    onClose: () -> Unit,
    onStartWorkout: () -> Unit,
    onAddExercisesClick: () -> Unit,
    state: WorkoutDayDetailState,
    userIntent: (WorkoutIntent) -> Unit,
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isRefreshing = state.isLoading
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
            coroutineScope.launch {
                userIntent(WorkoutIntent.FetchWorkoutDayById(workoutId))
            }
        })

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {

        val exercises = state.workoutDay?.exercises ?: emptyList()
        if (exercises.isEmpty()) {
            NotFoundScreen(
                context = context,
                workoutId = workoutId,
            )
            Button(
                onClick = {
                    onAddExercisesClick()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    stringResource(id = R.string.add_exercises),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                AppBar(title = "Workout Detail")
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    stringResource(id = R.string.sets_and_reps),
                    style = MaterialTheme.typography.h4
                )
                Spacer(modifier = Modifier.height(24.dp))
                LazyColumn {
                    items(exercises.size) {
                        ListTile(
                            title = exercises[it].name,
                            subtitle = "${exercises[it].baseSets} sets x ${exercises[it].baseReps} reps",
                            suffix = {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                }
                            })
                    }
                    item {
                        ListTile(title = "Add", suffix = {
                            IconButton(onClick = {
                                onAddExercisesClick()
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add")
                            }
                        })
                    }
                }
            }

            Button(
                onClick = {
                    onStartWorkout()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    stringResource(id = R.string.start_workout),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            PullRefreshIndicator(
                isRefreshing,
                pullRefreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun NotFoundScreen(
    context: Context,
    workoutId:String,
){
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        AppBar(title = workoutId.toIntOrNull()?.toDayOfTheWeekString(context)?: "")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.empty_workout_day),
            )
        }
    }
}