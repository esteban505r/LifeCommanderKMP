package com.esteban.ruano.nutrition_presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.text.TitleH3
import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_presentation.intent.MealTrackingIntent
import com.esteban.ruano.nutrition_presentation.ui.composables.RecipeComposable
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.MealTrackingState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MealTrackingScreen(
    onNavigateUp: () -> Unit,
    userIntent: (MealTrackingIntent) -> Unit,
    onRecipeClick: (String) -> Unit,
    state: MealTrackingState
) {
    var showAlternativeMealDialog by remember { mutableStateOf<Recipe?>(null) }
    var showSkippedMealDialog by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                onClick = { userIntent(MealTrackingIntent.RefreshMeals) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    stringResource(id = R.string.refresh),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(24.dp)
        ) {
            item {
                Text(
                    stringResource(id = R.string.todays_meals),
                    style = MaterialTheme.typography.h2,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.todayRecipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_meals_today),
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            } else {
                items(state.todayRecipes) { recipe ->
                    RecipeComposable(
                        recipe = recipe,
                        showDay = false,
                        onClick = { onRecipeClick(recipe.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Meal tracking actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                userIntent(MealTrackingIntent.TrackMealConsumed(recipe.id))
                            },
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        ) {
                            Text(stringResource(R.string.consumed))
                        }
                        
                        Button(
                            onClick = { showSkippedMealDialog = recipe },
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.secondary
                            )
                        ) {
                            Text(stringResource(R.string.skipped))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Alternative meal dialog
    showAlternativeMealDialog?.let { recipe ->
        AlternativeMealDialog(
            recipe = recipe,
            onDismiss = { showAlternativeMealDialog = null },
            onConfirm = { alternativeRecipeId, alternativeMealName ->
                userIntent(MealTrackingIntent.TrackMealSkippedWithAlternative(
                    recipeId = recipe.id,
                    alternativeRecipeId = alternativeRecipeId,
                    alternativeMealName = alternativeMealName
                ))
                showAlternativeMealDialog = null
            }
        )
    }

    // Skipped meal dialog
    showSkippedMealDialog?.let { recipe ->
        SkippedMealDialog(
            recipe = recipe,
            onDismiss = { showSkippedMealDialog = null },
            onConfirm = { alternativeRecipeId, alternativeMealName ->
                if (alternativeRecipeId != null || alternativeMealName != null) {
                    userIntent(MealTrackingIntent.TrackMealSkippedWithAlternative(
                        recipeId = recipe.id,
                        alternativeRecipeId = alternativeRecipeId,
                        alternativeMealName = alternativeMealName
                    ))
                } else {
                    userIntent(MealTrackingIntent.TrackMealSkipped(recipe.id))
                }
                showSkippedMealDialog = null
            }
        )
    }
}

@Composable
private fun AlternativeMealDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    onConfirm: (String?, String?) -> Unit
) {
    var alternativeMealName by remember { mutableStateOf("") }
    var selectedRecipeId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alternative Meal for ${recipe.name}") },
        text = {
            Column {
                Text("What did you eat instead?")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = alternativeMealName,
                    onValueChange = { alternativeMealName = it },
                    label = { Text("Meal name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedRecipeId, alternativeMealName.takeIf { it.isNotBlank() })
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SkippedMealDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    onConfirm: (String?, String?) -> Unit
) {
    var alternativeMealName by remember { mutableStateOf("") }
    var selectedRecipeId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Skip ${recipe.name}") },
        text = {
            Column {
                Text("Did you eat something else instead? (Optional)")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = alternativeMealName,
                    onValueChange = { alternativeMealName = it },
                    label = { Text("Alternative meal name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedRecipeId, alternativeMealName.takeIf { it.isNotBlank() })
                }
            ) {
                Text("Skip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 