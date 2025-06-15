package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifecommander.models.dashboard.WorkoutDTO

@Composable
fun WorkoutSummary(
    todayWorkout: WorkoutDTO?,
    caloriesBurned: Int,
    workoutStreak: Int,
    weeklyWorkoutCompletion: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colors.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = todayWorkout?.name ?: "No workout planned",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
            }
            Divider()
            Text("Calories Burned: $caloriesBurned", style = MaterialTheme.typography.body2)
            Text("Workout Streak: $workoutStreak days", style = MaterialTheme.typography.body2)
            Spacer(Modifier.height(8.dp))
            Text("Weekly Workout Completion", style = MaterialTheme.typography.caption)
            LinearProgressIndicator(progress = weeklyWorkoutCompletion, color = Color(0xFF4CAF50), modifier = Modifier.fillMaxWidth())
        }
    }
} 