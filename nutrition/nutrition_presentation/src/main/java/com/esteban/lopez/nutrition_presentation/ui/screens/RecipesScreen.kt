package com.esteban.ruano.nutrition_presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.nutrition_presentation.intent.RecipesIntent
import com.esteban.ruano.nutrition_presentation.ui.composables.RecipeComposable
import com.esteban.ruano.nutrition_presentation.ui.composables.RecipeFiltersDialog
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.RecipesState
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.ViewMode
import java.time.DayOfWeek

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipesScreen(
    onNavigateUp: () -> Unit,
    userIntent: (RecipesIntent) -> Unit,
    onNewRecipe: () -> Unit,
    onDetailRecipe: (String) -> Unit,
    state: RecipesState
) {
    val context = LocalContext.current
    var showFiltersDialog by remember { mutableStateOf(false) }
    
    // Auto-select current day when entering the screen in PLAN view mode
    LaunchedEffect(Unit) {
        if (state.viewMode == ViewMode.PLAN && state.daySelected == 0) {
            val currentDay = java.time.LocalDate.now().dayOfWeek.value
            userIntent(RecipesIntent.GetRecipesByDay(currentDay))
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                onClick = { onNewRecipe() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    stringResource(id = R.string.new_recipe_title),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = when (state.viewMode) {
                        ViewMode.PLAN -> "Daily Meal Plan"
                        ViewMode.DATABASE -> "Recipe Database"
                        ViewMode.HISTORY -> "Meal History for ${state.historicalDate ?: "Unknown Date"}"
                    },
                    style = MaterialTheme.typography.h4.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // View Mode Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.viewMode == ViewMode.PLAN,
                        onClick = { 
                            userIntent(RecipesIntent.SetViewMode(ViewMode.PLAN))
                            userIntent(RecipesIntent.GetRecipesByDay(java.time.LocalDate.now().dayOfWeek.value))
                        },
                        content = { Text("Daily Plan") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Daily Plan") }
                    )
                    FilterChip(
                        selected = state.viewMode == ViewMode.DATABASE,
                        onClick = { 
                            userIntent(RecipesIntent.SetViewMode(ViewMode.DATABASE))
                            userIntent(RecipesIntent.GetAllRecipes)
                        },
                        content = { Text("Database") },
                        leadingIcon = { Icon(Icons.Default.Storage, contentDescription = "Database") }
                    )
                    FilterChip(
                        selected = state.viewMode == ViewMode.HISTORY,
                        onClick = { 
                            userIntent(RecipesIntent.SetViewMode(ViewMode.HISTORY))
                            // TODO: Add date picker for history
                        },
                        content = { Text("History") },
                        leadingIcon = { Icon(Icons.Default.History, contentDescription = "History") }
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { userIntent(RecipesIntent.SearchRecipes(it)) },
                    label = { Text("Search recipes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { userIntent(RecipesIntent.ClearSearch) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Filters Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showFiltersDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Filters")
                    }
                    
                    // Clear All Filters Button (only show if filters are active)
                    if (state.recipeFilters != com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters()) {
                        OutlinedButton(
                            onClick = { userIntent(RecipesIntent.ClearAllFilters) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear all filters")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear")
                        }
                    }
                }
            }

            // Active Filters Display
            if (state.recipeFilters != com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Active filters:", style = MaterialTheme.typography.caption)
                        
                        // Sort indicator
                        if (state.recipeFilters.sortField != com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.NAME || 
                            state.recipeFilters.sortOrder != com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder.NONE) {
                            FilterChip(
                                selected = true,
                                onClick = { showFiltersDialog = true },
                                content = {
                                    Text("Sort: ${state.recipeFilters.sortField.name.lowercase().replaceFirstChar { it.uppercase() }} ${state.recipeFilters.sortOrder.name.lowercase().replaceFirstChar { it.uppercase() }}") 
                                },
                                leadingIcon = { Icon(Icons.Default.Sort, contentDescription = "Sort") }
                            )
                        }
                        
                        // Meal tag filter
                        state.recipeFilters.mealTypes?.forEach { tag ->
                            FilterChip(
                                selected = true,
                                onClick = { showFiltersDialog = true },
                                content = { Text(tag.capitalize()) },
                                leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = "Meal type") }
                            )
                        }
                        
                        // Day filter
                        state.recipeFilters.days?.forEach { day ->
                            val dayName = when (day) {
                                "1" -> "Monday"
                                "2" -> "Tuesday"
                                "3" -> "Wednesday"
                                "4" -> "Thursday"
                                "5" -> "Friday"
                                "6" -> "Saturday"
                                "7" -> "Sunday"
                                else -> day
                            }
                            FilterChip(
                                selected = true,
                                onClick = { showFiltersDialog = true },
                                content = { Text(dayName) },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Day") }
                            )
                        }
                        
                        // Nutrition filters
                        val nutritionFilters = listOf(
                            "calories" to (state.recipeFilters.minCalories to state.recipeFilters.maxCalories),
                            "protein" to (state.recipeFilters.minProtein to state.recipeFilters.maxProtein),
                            "carbs" to (state.recipeFilters.minCarbs to state.recipeFilters.maxCarbs),
                            "fat" to (state.recipeFilters.minFat to state.recipeFilters.maxFat),
                            "fiber" to (state.recipeFilters.minFiber to state.recipeFilters.maxFiber),
                            "sugar" to (state.recipeFilters.minSugar to state.recipeFilters.maxSugar),
                            "sodium" to (state.recipeFilters.minSodium to state.recipeFilters.maxSodium)
                        )
                        
                        nutritionFilters.forEach { (type, range) ->
                            val (minValue, maxValue) = range
                            if (minValue != null || maxValue != null) {
                                val label = when {
                                    minValue != null && maxValue != null -> "$type: $minValue-$maxValue"
                                    minValue != null -> "$type: ≥$minValue"
                                    maxValue != null -> "$type: ≤$maxValue"
                                    else -> type
                                }
                                FilterChip(
                                    selected = true,
                                    onClick = { showFiltersDialog = true },
                                    content = { Text(label.capitalize()) },
                                    leadingIcon = { Icon(Icons.Default.LocalDining, contentDescription = "Nutrition filter") }
                                )
                            }
                        }
                    }
                }
            }

            // Day chips for Plan view
            if (state.viewMode == ViewMode.PLAN) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            val dayNumber = day.value
                            val isSelected = state.daySelected == dayNumber
                            
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    userIntent(RecipesIntent.GetRecipesByDay(dayNumber))
                                },
                                content = { 
                                    Text(day.name.lowercase().replaceFirstChar { it.uppercase() })
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                } else null
                            )
                        }
                    }
                }
            }

            // Loading state
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.recipes?.recipes?.isEmpty() == true) {
                // Empty state
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = "No meals",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                when {
                                    state.searchQuery.isNotEmpty() -> "No recipes found matching '${state.searchQuery}'"
                                    state.recipeFilters != com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters() -> "No recipes match the current filters"
                                    state.viewMode == ViewMode.PLAN -> "No meals planned for this day."
                                    state.viewMode == ViewMode.DATABASE -> "No recipes in the database."
                                    state.viewMode == ViewMode.HISTORY -> "No meals tracked for this day."
                                    else -> "No recipes found."
                                },
                                style = MaterialTheme.typography.body1.copy(
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            } else {
                // Recipe list
                state.recipes?.recipes?.forEach { recipe ->
                    item {
                        RecipeComposable(
                            recipe = recipe,
                            showDay = state.viewMode == ViewMode.DATABASE,
                            onClick = { onDetailRecipe(recipe.id) }
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Filters Dialog
    RecipeFiltersDialog(
        show = showFiltersDialog,
        onDismiss = { showFiltersDialog = false },
        onApplyFilters = { filters ->
            userIntent(RecipesIntent.ApplyFilters(filters))
            showFiltersDialog = false
        },
        currentFilters = state.recipeFilters
    )
}