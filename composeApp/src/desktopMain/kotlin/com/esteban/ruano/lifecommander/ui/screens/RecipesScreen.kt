package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.ui.state.RecipesState
import java.time.DayOfWeek

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipesScreen(
    state: RecipesState,
    onNewRecipe: () -> Unit,
    onDetailRecipe: (String) -> Unit,
    onGetRecipesByDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysOfWeek = DayOfWeek.values()
    val selectedDay = state.daySelected
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNewRecipe) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Meals by Day",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                /*FilterChip(
                    selected = selectedDay == 0,
                    onClick = { onGetRecipesByDay(
                        0
                    ) },
                    colors = ChipDefaults.filterChipColors(
                        backgroundColor = if (selectedDay == 0) MaterialTheme.colors.primary.copy(alpha = 0.15f) else MaterialTheme.colors.surface,
                        contentColor = MaterialTheme.colors.primary
                    )
                ) {
                    Text("All")
                }*/
                daysOfWeek.forEach { day ->
                    FilterChip(
                        selected = selectedDay == day.value,
                        onClick = { onGetRecipesByDay(day.value) },
                        colors = ChipDefaults.filterChipColors(
                            backgroundColor = if (selectedDay == day.value) MaterialTheme.colors.primary.copy(alpha = 0.15f) else MaterialTheme.colors.surface,
                            contentColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text(day.name)
                    }
                }
            }
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (state.recipes.isEmpty()) {
                Text("No meals found.", style = MaterialTheme.typography.body1)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.recipes.size) { index ->
                        val recipe = state.recipes[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onDetailRecipe(recipe.id) },
                            elevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(recipe.name, style = MaterialTheme.typography.subtitle1)
                                if (!recipe.note.isNullOrBlank()) {
                                    Text(recipe.note!!, style = MaterialTheme.typography.body2)
                                }
                                if (recipe.mealTag != null) {
                                    Text("Type: ${recipe.mealTag}", style = MaterialTheme.typography.caption)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 