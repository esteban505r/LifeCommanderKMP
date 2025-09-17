package com.esteban.ruano.workout_presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.ListTile
import com.esteban.ruano.core_ui.utils.DateUIUtils.DAYS_OF_THE_WEEK
import com.esteban.ruano.core_ui.utils.DateUIUtils.toDayOfTheWeekString
import com.esteban.ruano.resources.Res
import com.esteban.ruano.ui.DarkGray
import com.esteban.ruano.ui.LightGray
import com.esteban.ruano.ui.LightGray4
import com.esteban.ruano.resources.otter_working_out
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.composable.WorkoutProgressSummary
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutState
import java.time.LocalDate

@Composable
fun WorkoutScreen(
    onNavigateUp: () -> Unit,
    onWorkoutClick: (id: Int) -> Unit,
    onExercisesClick: () -> Unit,
    userIntent: (WorkoutIntent) -> Unit,
    state: WorkoutState,
) {

    val context = LocalContext.current

    val today =
        state.workouts.firstOrNull { it.day == LocalDate.now().dayOfWeek.value }

    Scaffold (
    ) {
        Box {
            state.errorMessage?.let {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(it)
                }
                return@Box
            }

            if(state.isLoading){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Box
            }

            LazyColumn(
                modifier = Modifier
                    .padding(24.dp)
                    .padding(it)
            ) {
                item {
                    Box {
                        Image(
                            modifier = Modifier.align(Alignment.TopEnd).size(90.dp).rotate(-20f),
                            painter = painterResource(R.drawable.workout_hydration_jug),
                            contentDescription = "Workout Decoration",
                        )
                        Column {
                            Text(
                                stringResource(id = R.string.workout),
                                style = MaterialTheme.typography.h2,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                modifier = Modifier.clickable { onExercisesClick() }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "You have ${state.totalExercises} exercises",
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Progress Summary
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    WorkoutProgressSummary(
                        totalExercises = today?.exercises?.size ?: 0,
                        completedExercises = state.completedExercises.size,
                        weeklyWorkoutsCompleted = state.weeklyWorkoutsCompleted.size
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                item {
                    if (today != null && today.exercises.isNotEmpty()) {
                        Text(
                            stringResource(id = R.string.todays_workout),
                            style = MaterialTheme.typography.h3,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Enhanced today's workout card
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable{
                                onWorkoutClick(today.day)
                            },
                            shape = RoundedCornerShape(16.dp),
                            backgroundColor = MaterialTheme.colors.surface,
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Image(
                                    painter = org.jetbrains.compose.resources.painterResource(Res.drawable.otter_working_out),
                                    contentDescription = "Pushups",
                                    modifier = Modifier
                                        .height(200.dp)
                                        .fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    today.name, 
                                    style = MaterialTheme.typography.h4.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "${today.exercises.first().baseSets} sets of ${today.exercises.first().baseReps} reps",
                                    style = MaterialTheme.typography.body1.copy(color = LightGray)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "90 minutes",
                                    style = MaterialTheme.typography.body1.copy(color = LightGray)
                                )
                                
                                // Show completion status
                                if (state.completedExercises.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = MaterialTheme.colors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "${state.completedExercises.size} of ${today.exercises.size} exercises completed",
                                            style = MaterialTheme.typography.body2.copy(
                                                color = MaterialTheme.colors.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Text("This week", style = MaterialTheme.typography.h3)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(DAYS_OF_THE_WEEK) { dayIndex ->
                    val day = dayIndex + 1
                    val workoutDay = state.workouts.firstOrNull { it.day == day }
                    val isCompleted = state.weeklyWorkoutsCompleted.contains(day)
                    
                    ListTile(
                        title = day.toDayOfTheWeekString(context),
                        subtitle = workoutDay?.name ?: stringResource(R.string.free_day),
                        prefix = {
                            Card(
                                elevation = 0.dp,
                                modifier = Modifier.padding(end = 16.dp),
                                backgroundColor = if (isCompleted) 
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                                else 
                                    LightGray4
                            ) {
                                Icon(
                                    if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                    contentDescription = if (isCompleted) "Completed" else "Play",
                                    tint = if (isCompleted) MaterialTheme.colors.primary else DarkGray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        },
                        onClick = {
                            onWorkoutClick(day)
                        }
                    )
                }
            }
        }
    }

}