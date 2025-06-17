package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.ui.state.RecipesState
import java.time.DayOfWeek
import ui.composables.NewEditRecipeDialog

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipesScreen(
    state: RecipesState,
    onNewRecipe: () -> Unit,
    onDetailRecipe: (String) -> Unit,
    onGetRecipesByDay: (Int) -> Unit,
    onEditRecipe: (Recipe) -> Unit = {},
    onDeleteRecipe: (String) -> Unit = {},
    onConsumeRecipe: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showRecipeDialog by remember { mutableStateOf(false) }
    var recipeToEdit by remember { mutableStateOf<Recipe?>(null) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val daysOfWeek = DayOfWeek.values()
    val selectedDay = state.daySelected
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Add Recipe button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meals by Day",
                style = MaterialTheme.typography.h4
            )
            Button(
                onClick = {
                    recipeToEdit = null
                    showRecipeDialog = true
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Recipe")
            }
        }
        // Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                            .padding(vertical = 4.dp),
                        elevation = 2.dp,
                        backgroundColor = if (recipe.consumed) MaterialTheme.colors.surface else MaterialTheme.colors.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).clickable { onDetailRecipe(recipe.id) }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(recipe.name, style = MaterialTheme.typography.subtitle1)
                                        if (recipe.consumed) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Consumed",
                                                tint = MaterialTheme.colors.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    if (!recipe.note.isNullOrBlank()) {
                                        Text(recipe.note!!, style = MaterialTheme.typography.body2)
                                    }
                                    if (recipe.mealTag != null) {
                                        Text("Type: ${recipe.mealTag}", style = MaterialTheme.typography.caption)
                                    }
                                    if (recipe.consumed && !recipe.consumedDateTime.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Consumed: ${recipe.consumedDateTime}",
                                                style = MaterialTheme.typography.caption,
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                                Row {
                                    // Consume Recipe Button
                                    IconButton(
                                        onClick = { onConsumeRecipe(recipe.id) },
                                        modifier = Modifier.size(40.dp),
                                        enabled = !recipe.consumed
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = if (recipe.consumed) "Already Consumed" else "Consume Recipe",
                                            tint = if (recipe.consumed) MaterialTheme.colors.onSurface.copy(alpha = 0.3f) else MaterialTheme.colors.secondary
                                        )
                                    }
                                    IconButton(onClick = {
                                        recipeToEdit = recipe
                                        showRecipeDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Recipe")
                                    }
                                    IconButton(onClick = {
                                        recipeToDelete = recipe
                                        showDeleteDialog = true
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Recipe")
                                    }
                                }
                            }
                            
                            // Recipe stats row
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Protein: ${recipe.protein ?: 0}g",
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                                Text(
                                    "Day ${recipe.day ?: 0}",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Recipe Dialog
    NewEditRecipeDialog(
        recipeToEdit = recipeToEdit,
        show = showRecipeDialog,
        onDismiss = {
            showRecipeDialog = false
            recipeToEdit = null
        },
        onSave = { recipe ->
            if (recipeToEdit == null) {
                onNewRecipe()
            } else {
                onEditRecipe(recipe)
            }
            showRecipeDialog = false
            recipeToEdit = null
        }
    )

    // Delete Warning Dialog
    if (showDeleteDialog && recipeToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                recipeToDelete = null
            },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete '${recipeToDelete!!.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRecipe(recipeToDelete!!.id)
                        showDeleteDialog = false
                        recipeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        showDeleteDialog = false
                        recipeToDelete = null
                    }
                ) { Text("Cancel") }
            }
        )
    }
} 