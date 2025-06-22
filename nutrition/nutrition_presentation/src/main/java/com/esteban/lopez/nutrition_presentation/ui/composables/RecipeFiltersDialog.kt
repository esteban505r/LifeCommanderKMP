package com.esteban.ruano.nutrition_presentation.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters
import com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField
import com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipeFiltersDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onApplyFilters: (RecipeFilters) -> Unit,
    currentFilters: RecipeFilters = RecipeFilters()
) {
    if (show) {
        var filters by remember { mutableStateOf(currentFilters) }
        
        // Update local filters when currentFilters changes
        LaunchedEffect(currentFilters) {
            filters = currentFilters
        }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Recipe Filters & Sorting") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sorting Section
                    Section(title = "Sorting") {
                        // Sort By Dropdown
                        var sortByExpanded by remember { mutableStateOf(false) }
                        val sortOptions = RecipeSortField.entries.toList()
                        
                        ExposedDropdownMenuBox(
                            expanded = sortByExpanded,
                            onExpandedChange = { sortByExpanded = !sortByExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = filters.sortField.name.lowercase().replaceFirstChar { it.uppercase() },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Sort By") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortByExpanded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = sortByExpanded,
                                onDismissRequest = { sortByExpanded = false }
                            ) {
                                sortOptions.forEach { option ->
                                    DropdownMenuItem(
                                        onClick = {
                                            filters = filters.copy(sortField = option)
                                            sortByExpanded = false
                                        }
                                    ) {
                                        Text(text = option.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                }
                            }
                        }

                        // Sort Order Toggle
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Order:", style = MaterialTheme.typography.body1)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = filters.sortOrder == RecipeSortOrder.ASCENDING,
                                    onClick = { filters = filters.copy(sortOrder = RecipeSortOrder.ASCENDING) },
                                    content = { Text("Ascending") },
                                    leadingIcon = { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Ascending") }
                                )
                                FilterChip(
                                    selected = filters.sortOrder == RecipeSortOrder.DESCENDING,
                                    onClick = { filters = filters.copy(sortOrder = RecipeSortOrder.DESCENDING) },
                                    content = { Text("Descending") },
                                    leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Descending") }
                                )
                            }
                        }
                    }

                    // Meal Tag Filter Section
                    Section(title = "Meal Type Filter") {
                        var mealTagExpanded by remember { mutableStateOf(false) }
                        val mealTags = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
                        val selectedMealTypes = filters.mealTypes ?: emptyList()
                        
                        ExposedDropdownMenuBox(
                            expanded = mealTagExpanded,
                            onExpandedChange = { mealTagExpanded = !mealTagExpanded }
                        ) {
                            OutlinedTextField(
                                value = if (selectedMealTypes.isEmpty()) "All Meal Types" else selectedMealTypes.joinToString(", "),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Meal Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealTagExpanded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = mealTagExpanded,
                                onDismissRequest = { mealTagExpanded = false }
                            ) {
                                mealTags.forEach { tag ->
                                    val isSelected = selectedMealTypes.contains(tag)
                                    DropdownMenuItem(
                                        onClick = {
                                            val newSelection = if (isSelected) {
                                                selectedMealTypes - tag
                                            } else {
                                                selectedMealTypes + tag
                                            }
                                            filters = filters.copy(mealTypes = newSelection.takeIf { it.isNotEmpty() })
                                            mealTagExpanded = false
                                        }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = tag.capitalize())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Day Filter Section
                    Section(title = "Day Filter") {
                        var dayExpanded by remember { mutableStateOf(false) }
                        val days = listOf("1" to "Monday", "2" to "Tuesday", "3" to "Wednesday", "4" to "Thursday", "5" to "Friday", "6" to "Saturday", "7" to "Sunday")
                        val selectedDays = filters.days ?: emptyList()
                        
                        ExposedDropdownMenuBox(
                            expanded = dayExpanded,
                            onExpandedChange = { dayExpanded = !dayExpanded }
                        ) {
                            OutlinedTextField(
                                value = if (selectedDays.isEmpty()) "All Days" else selectedDays.mapNotNull { day -> days.find { it.first == day }?.second }.joinToString(", "),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Days") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = dayExpanded,
                                onDismissRequest = { dayExpanded = false }
                            ) {
                                days.forEach { (dayNum, dayName) ->
                                    val isSelected = selectedDays.contains(dayNum)
                                    DropdownMenuItem(
                                        onClick = {
                                            val newSelection = if (isSelected) {
                                                selectedDays - dayNum
                                            } else {
                                                selectedDays + dayNum
                                            }
                                            filters = filters.copy(days = newSelection.takeIf { it.isNotEmpty() })
                                            dayExpanded = false
                                        }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = dayName)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Nutrition Filters Section
                    Section(title = "Nutrition Filters") {
                        val nutritionTypes = listOf(
                            "calories" to "Calories (kcal)",
                            "protein" to "Protein (g)",
                            "carbs" to "Carbs (g)",
                            "fat" to "Fat (g)",
                            "fiber" to "Fiber (g)",
                            "sugar" to "Sugar (g)",
                            "sodium" to "Sodium (mg)"
                        )
                        
                        nutritionTypes.forEach { (type, label) ->
                            NutritionFilterRow(
                                label = label,
                                minValue = when (type) {
                                    "calories" -> filters.minCalories
                                    "protein" -> filters.minProtein
                                    "carbs" -> filters.minCarbs
                                    "fat" -> filters.minFat
                                    "fiber" -> filters.minFiber
                                    "sugar" -> filters.minSugar
                                    "sodium" -> filters.minSodium
                                    else -> null
                                },
                                maxValue = when (type) {
                                    "calories" -> filters.maxCalories
                                    "protein" -> filters.maxProtein
                                    "carbs" -> filters.maxCarbs
                                    "fat" -> filters.maxFat
                                    "fiber" -> filters.maxFiber
                                    "sugar" -> filters.maxSugar
                                    "sodium" -> filters.maxSodium
                                    else -> null
                                },
                                onFilterChange = { minValue, maxValue ->
                                    filters = when (type) {
                                        "calories" -> filters.copy(minCalories = minValue, maxCalories = maxValue)
                                        "protein" -> filters.copy(minProtein = minValue, maxProtein = maxValue)
                                        "carbs" -> filters.copy(minCarbs = minValue, maxCarbs = maxValue)
                                        "fat" -> filters.copy(minFat = minValue, maxFat = maxValue)
                                        "fiber" -> filters.copy(minFiber = minValue, maxFiber = maxValue)
                                        "sugar" -> filters.copy(minSugar = minValue, maxSugar = maxValue)
                                        "sodium" -> filters.copy(minSodium = minValue, maxSodium = maxValue)
                                        else -> filters
                                    }
                                }
                            )
                            if (type != nutritionTypes.last().first) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onApplyFilters(filters)
                        onDismiss()
                    }
                ) {
                    Text("Apply Filters")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NutritionFilterRow(
    label: String,
    minValue: Double?,
    maxValue: Double?,
    onFilterChange: (Double?, Double?) -> Unit
) {
    var minValueText by remember { mutableStateOf(minValue?.toString() ?: "") }
    var maxValueText by remember { mutableStateOf(maxValue?.toString() ?: "") }
    
    // Sync text fields with current values
    LaunchedEffect(minValue, maxValue) {
        minValueText = minValue?.toString() ?: ""
        maxValueText = maxValue?.toString() ?: ""
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = minValueText,
                onValueChange = { 
                    minValueText = it
                    onFilterChange(it.toDoubleOrNull(), maxValueText.toDoubleOrNull())
                },
                label = { Text("Min") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            Text("to", style = MaterialTheme.typography.body2)
            
            OutlinedTextField(
                value = maxValueText,
                onValueChange = { 
                    maxValueText = it
                    onFilterChange(minValueText.toDoubleOrNull(), it.toDoubleOrNull())
                },
                label = { Text("Max") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            IconButton(
                onClick = {
                    minValueText = ""
                    maxValueText = ""
                    onFilterChange(null, null)
                }
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear filter")
            }
        }
    }
}

@Composable
fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            content()
        }
    }
} 