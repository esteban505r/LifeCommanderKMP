package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifecommander.models.dashboard.MealDTO

@Composable
fun MealsSummary(
    todayCalories: Int,
    mealsLogged: Int,
    nextMeal: MealDTO?,
    weeklyMealLogging: Float,
    plannedMeals: Int = 0,
    unexpectedMeals: Int = 0
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
                Icon(Icons.Default.Fastfood, contentDescription = null, tint = MaterialTheme.colors.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Calories: $todayCalories | Meals: $mealsLogged",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
            }
            Divider()
            
            // Show planned vs unexpected meals breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Planned: $plannedMeals",
                    style = MaterialTheme.typography.body2,
                    color = Color(0xFFFFA726)
                )
                Text(
                    text = "Unexpected: $unexpectedMeals",
                    style = MaterialTheme.typography.body2,
                    color = Color(0xFFFF5722)
                )
            }
            
            if (nextMeal != null) {
                Text("Next Meal: ${nextMeal.name} at ${nextMeal.time} (${nextMeal.calories} kcal)", style = MaterialTheme.typography.body2)
            } else {
                Text("No more meals planned today", style = MaterialTheme.typography.body2)
            }
            Spacer(Modifier.height(8.dp))
            Text("Weekly Meal Logging", style = MaterialTheme.typography.caption)
            LinearProgressIndicator(progress = weeklyMealLogging, color = Color(0xFF2196F3), modifier = Modifier.fillMaxWidth())
        }
    }
} 