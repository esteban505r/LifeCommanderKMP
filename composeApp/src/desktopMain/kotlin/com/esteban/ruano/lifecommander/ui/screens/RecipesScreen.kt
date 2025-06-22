package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters
import com.esteban.ruano.lifecommander.ui.composables.NewEditRecipeDialog
import com.esteban.ruano.lifecommander.ui.composables.RecipeFiltersDialog
import com.esteban.ruano.lifecommander.ui.state.RecipesState
import com.esteban.ruano.lifecommander.ui.state.ViewMode
import kotlinx.datetime.*
import java.time.DayOfWeek

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipesScreen(
    state: RecipesState,
    onGetRecipesByDay: (Int) -> Unit,
    onGetAllRecipes: () -> Unit,
    onGetHistoryForDate: (LocalDate) -> Unit,
    onNewRecipe: (Recipe) -> Unit,
    onEditRecipe: (Recipe) -> Unit,
    onDeleteRecipe: (String) -> Unit,
    onConsumeRecipe: (String) -> Unit,
    onSkipRecipeWithAlternative: (String, String?, String?) -> Unit,
    onSearchRecipes: (String) -> Unit,
    onClearSearch: () -> Unit,
    onApplyFilters: (RecipeFilters) -> Unit,
    onClearAllFilters: () -> Unit
) {
    var showRecipeDialog by remember { mutableStateOf(false) }
    var recipeToEdit by remember { mutableStateOf<Recipe?>(null) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSkipDialog by remember { mutableStateOf<Recipe?>(null) }
    var alternativeMealName by remember { mutableStateOf(TextFieldValue("")) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showFiltersDialog by remember { mutableStateOf(false) }
    
    val daysOfWeek = remember { DayOfWeek.values() }
    
    Column(
        modifier = Modifier
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
                text = when (state.viewMode) {
                    ViewMode.PLAN -> "Daily Meal Plan"
                    ViewMode.DATABASE -> "Recipe Database"
                    ViewMode.HISTORY -> "Meal History for ${state.historicalDate}"
                },
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Button(
                onClick = {
                    recipeToEdit = null
                    showRecipeDialog = true
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Recipe")
            }
        }
        
        // View Mode Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.viewMode == ViewMode.PLAN,
                onClick = { 
                    val todayIndex = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.value
                    onGetRecipesByDay(todayIndex)
                },
                colors = ChipDefaults.filterChipColors(
                    backgroundColor = if (state.viewMode == ViewMode.PLAN) MaterialTheme.colors.secondary.copy(alpha = 0.15f) else MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.secondary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Schedule, contentDescription = "Daily Plan", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Daily Plan")
            }
            FilterChip(
                selected = state.viewMode == ViewMode.DATABASE,
                onClick = { onGetAllRecipes() },
                colors = ChipDefaults.filterChipColors(
                    backgroundColor = if (state.viewMode == ViewMode.DATABASE) MaterialTheme.colors.secondary.copy(alpha = 0.15f) else MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.secondary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Storage, contentDescription = "Database", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Database")
            }
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.History, contentDescription = "View History")
            }
        }

        // Search and Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onSearchRecipes(it) },
                label = { Text("Search recipes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            // Filters Button
            OutlinedButton(
                onClick = { showFiltersDialog = true },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filters")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Filters")
            }
            
            // Clear All Filters Button (only show if filters are active)
            if (state.recipeFilters != RecipeFilters()) {
                OutlinedButton(
                    onClick = onClearAllFilters,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear all filters")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }

        // Active Filters Display
        if (state.recipeFilters != RecipeFilters()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Active filters:", style = MaterialTheme.typography.caption, modifier = Modifier.padding(top = 8.dp))
                
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

        // Day chips for Plan view
        if (state.viewMode == ViewMode.PLAN) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            daysOfWeek.forEach { day ->
                FilterChip(
                        selected = state.daySelected == day.value,
                    onClick = { onGetRecipesByDay(day.value) },
                    colors = ChipDefaults.filterChipColors(
                            backgroundColor = if (state.daySelected == day.value) MaterialTheme.colors.primary.copy(alpha = 0.15f) else MaterialTheme.colors.surface,
                        contentColor = MaterialTheme.colors.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                        Text(day.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }
        
        // Content area - this is what gets affected by loading state
        Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            } else if (state.recipes?.recipes?.isEmpty() == true)  {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                                state.recipeFilters != RecipeFilters() -> "No recipes match the current filters"
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
        } else {
                // Recipe list content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                    state.recipes?.recipes?.forEach { recipe ->
                        item {
                            val todayIndex = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.value
                            val actionsEnabled = state.viewMode == ViewMode.PLAN && state.daySelected == todayIndex
                            val showDayAssignment = state.viewMode == ViewMode.DATABASE
                            
                            RecipeCard(
                                recipe = recipe,
                                isConsumed = recipe.consumed,
                                actionsEnabled = actionsEnabled,
                                showDayAssignment = showDayAssignment,
                                onEdit = {
                                        recipeToEdit = recipe
                                        showRecipeDialog = true
                                            },
                                onDelete = {
                                        recipeToDelete = recipe
                                        showDeleteDialog = true
                                            },
                                onConsumeRecipe = { onConsumeRecipe(recipe.id) },
                                onSkipRecipe = { showSkipDialog = recipe }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                showDatePicker = false
                onGetHistoryForDate(date)
            }
        )
    }

    // Recipe Dialog
    if (showRecipeDialog) {
    NewEditRecipeDialog(
        recipeToEdit = recipeToEdit,
        show = showRecipeDialog,
            onDismiss = { showRecipeDialog = false },
        onSave = { recipe ->
                if (recipeToEdit != null) {
                    onEditRecipe(recipe)
            } else {
                    onNewRecipe(recipe)
                }
                showRecipeDialog = false
            }
        )
    }

    // Filters Dialog
    RecipeFiltersDialog(
        show = showFiltersDialog,
        onDismiss = { showFiltersDialog = false },
        onApplyFilters = onApplyFilters,
        currentFilters = state.recipeFilters
    )

    // Delete Confirmation Dialog
    if (showDeleteDialog && recipeToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete '${recipeToDelete!!.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRecipe(recipeToDelete!!.id)
                        showDeleteDialog = false
                        recipeToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Skip Recipe Dialog
    showSkipDialog?.let { recipe ->
        var searchQuery by remember { mutableStateOf("") }
        var filteredRecipes = state.recipes?.recipes?.filter {
            it.id != recipe.id && (it.name.contains(searchQuery, ignoreCase = true) || searchQuery.isBlank())
        }
        var expanded by remember { mutableStateOf(false) }
        var selectedReplacement: Recipe? by remember { mutableStateOf(null) }
        AlertDialog(
            onDismissRequest = { showSkipDialog = null },
            title = { Text("Skip ${recipe.name}") },
            text = {
                Column {
                    Text("You must specify what you ate instead.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search recipes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = alternativeMealName,
                        onValueChange = { alternativeMealName = it },
                        label = { Text("Or enter meal name") },
                            modifier = Modifier.fillMaxWidth()
                    )
                    if (filteredRecipes?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Or select from your recipes:", style = MaterialTheme.typography.caption)
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredRecipes?:listOf()) { replacementRecipe ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedReplacement = replacementRecipe },
                                    backgroundColor = if (selectedReplacement?.id == replacementRecipe.id) 
                                        MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                                    else 
                                        MaterialTheme.colors.surface
                                ) {
                                    Text(
                                        replacementRecipe.name,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val alternativeId = selectedReplacement?.id
                        val alternativeName = if (alternativeId == null) alternativeMealName.text else null
                        onSkipRecipeWithAlternative(recipe.id, alternativeId, alternativeName)
                        showSkipDialog = null
                        selectedReplacement = null
                        alternativeMealName = TextFieldValue("")
                    },
                    enabled = selectedReplacement != null || alternativeMealName.text.isNotBlank()
                ) {
                    Text("Skip")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                        showSkipDialog = null
                    selectedReplacement = null
                        alternativeMealName = TextFieldValue("")
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Month/Year header with navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedDate = selectedDate.minus(1, DateTimeUnit.MONTH) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                    }
                    Text(
                        text = "${selectedDate.month.name} ${selectedDate.year}",
                        style = MaterialTheme.typography.h6
                    )
                    IconButton(onClick = { selectedDate = selectedDate.plus(1, DateTimeUnit.MONTH) }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Day of week headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                    days.forEach { day ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(text = day, style = MaterialTheme.typography.caption)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Calendar grid
                val firstDayOfMonth = LocalDate(selectedDate.year, selectedDate.month, 1)
                val firstDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber % 7 // Sunday is 0
                val daysInMonth = selectedDate.month.length(isLeapYear(selectedDate.year))

                Column {
                    val rows = (daysInMonth + firstDayOfWeek + 6) / 7
                    repeat(rows) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(7) { col ->
                                val day = row * 7 + col - firstDayOfWeek + 1
                                if (day in 1..daysInMonth) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                if (day == selectedDate.dayOfMonth) MaterialTheme.colors.primary else Color.Transparent
                                            )
                                            .clickable {
                                                selectedDate = LocalDate(selectedDate.year, selectedDate.month, day)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            color = if (day == selectedDate.dayOfMonth) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // OK and Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onDateSelected(selectedDate) }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

private fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCard(
    recipe: Recipe,
    isConsumed: Boolean,
    actionsEnabled: Boolean,
    showDayAssignment: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onConsumeRecipe: () -> Unit,
    onSkipRecipe: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 6.dp,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = if (isConsumed)
            MaterialTheme.colors.surface.copy(alpha = 0.8f)
        else
            MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top section: Icon, Name, Note, Day Assignment
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recipe icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            when (recipe.mealTag?.uppercase()) {
                                "BREAKFAST" -> Color(0xFFFFB74D)
                                "LUNCH" -> Color(0xFF81C784)
                                "DINNER" -> Color(0xFF64B5F6)
                                "SNACK" -> Color(0xFFFF8A65)
                                else -> Color(0xFFE0E0E0)
                            }.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (recipe.mealTag?.uppercase()) {
                            "BREAKFAST" -> Icons.Default.Restaurant
                            "LUNCH" -> Icons.Default.LocalDining
                            "DINNER" -> Icons.Default.Fastfood
                            else -> Icons.Default.Fastfood
                        },
                        contentDescription = "Food",
                        tint = when (recipe.mealTag?.uppercase()) {
                            "BREAKFAST" -> Color(0xFFFF8F00)
                            "LUNCH" -> Color(0xFF4CAF50)
                            "DINNER" -> Color(0xFF2196F3)
                            "SNACK" -> Color(0xFFFF5722)
                            else -> Color(0xFF757575)
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            recipe.name,
                            style = MaterialTheme.typography.h6.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isConsumed)
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                else
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.95f)
                            ),
                            maxLines = 1
                        )
                        if (isConsumed) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Consumed",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (!recipe.note.isNullOrBlank()) {
                        Text(
                            recipe.note ?: "",
                            style = MaterialTheme.typography.body2.copy(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            ),
                            maxLines = 1
                        )
                    }
                    if (showDayAssignment && recipe.days?.isNotEmpty() == true) {
                        Text(
                            "Assigned to ${recipe.days?.sorted()?.joinToString(", ") {
                                kotlinx.datetime.DayOfWeek(it).name.lowercase().replaceFirstChar { char -> char.uppercase() }
                            }}",
                            style = MaterialTheme.typography.caption.copy(
                                color = MaterialTheme.colors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Nutritional Info
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                NutrientInfo("Calories", recipe.calories, Icons.Default.LocalFireDepartment)
                NutrientInfo("Protein", recipe.protein, Icons.Default.FitnessCenter)
                NutrientInfo("Carbs", recipe.carbs, Icons.Default.Grain)
                NutrientInfo("Fat", recipe.fat, Icons.Default.Fastfood)
                NutrientInfo("Fiber", recipe.fiber, Icons.Default.Grass)
                NutrientInfo("Sugar", recipe.sugar, Icons.Default.Cake)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ingredient and Instruction Counts
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (recipe.ingredients.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ListAlt, contentDescription = "Ingredients", modifier = Modifier.size(16.dp), tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                            Text(
                                text = "${recipe.ingredients.size} Ingredients",
                                style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f))
                            )
                        }
                    }
                    if (recipe.instructions.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.FormatListNumbered, contentDescription = "Instructions", modifier = Modifier.size(16.dp), tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                            Text(
                                text = "${recipe.instructions.size} Steps",
                                style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f))
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (actionsEnabled) {
                        IconButton(onClick = onConsumeRecipe, enabled = !isConsumed) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = if (isConsumed) "Already Consumed" else "Consume Recipe",
                                tint = if (isConsumed) MaterialTheme.colors.onSurface.copy(alpha = 0.3f) else Color(0xFF4CAF50)
                            )
                        }
                        IconButton(onClick = onSkipRecipe, enabled = !isConsumed) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Skip Recipe",
                                tint = if (isConsumed) MaterialTheme.colors.onSurface.copy(alpha = 0.3f) else Color(0xFFFF9800)
                            )
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit Recipe", tint = MaterialTheme.colors.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete Recipe", tint = MaterialTheme.colors.error)
                    }
                }
            }
        }
    }
}

@Composable
fun NutrientInfo(name: String, value: Double?, icon: ImageVector) {
    if (value != null && value > 0) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.05f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "$value${if (name != "Calories") "g" else "kcal"} $name",
                    style = MaterialTheme.typography.caption.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
} 