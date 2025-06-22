package com.esteban.ruano.lifecommander.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.esteban.ruano.lifecommander.models.Ingredient
import com.esteban.ruano.lifecommander.models.Instruction
import com.esteban.ruano.lifecommander.models.Recipe
import java.time.DayOfWeek
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewEditRecipeDialog(
    recipeToEdit: Recipe?,
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: (Recipe) -> Unit
) {
    var name by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.name ?: "") }
    var note by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.note ?: "") }
    var protein by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.protein?.toString() ?: "") }
    var calories by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.calories?.toString() ?: "") }
    var carbs by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.carbs?.toString() ?: "") }
    var fat by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.fat?.toString() ?: "") }
    var fiber by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.fiber?.toString() ?: "") }
    var sugar by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.sugar?.toString() ?: "") }
    var selectedDays by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.days ?: emptyList()) }
    var selectedMealTag by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.mealTag ?: "BREAKFAST") }
    var ingredients by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.ingredients?.takeIf { it.isNotEmpty() } ?: listOf(Ingredient(name = "", quantity = 0.0, unit = ""))) }
    var instructions by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.instructions?.takeIf { it.isNotEmpty() } ?: listOf(Instruction(stepNumber = 1, description = ""))) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val dialogState = rememberDialogState(width = 800.dp, height = 900.dp)

    if (show) {
        DialogWindow(
            onCloseRequest = onDismiss,
            state = dialogState,
            visible = show,
            title = if (recipeToEdit == null) "Create New Recipe" else "Edit Recipe"
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    TopAppBar(
                        title = { Text(if (recipeToEdit == null) "Create New Recipe" else "Edit Recipe") },
                        actions = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close Dialog")
                            }
                        },
                        elevation = 4.dp
                    )

                    // Main Content
                    Box(modifier = Modifier.weight(1f)) {
                        val scrollState = rememberScrollState()
            Column(
                        modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // General Info Section
                            Section(title = "General Information") {
                                OutlinedTextField(value = name, onValueChange = { name = it; nameError = null }, label = { Text("Recipe Name*") }, modifier = Modifier.fillMaxWidth(), isError = nameError != null, singleLine = true)
                                if (nameError != null) {
                                    Text(nameError!!, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                                }
                                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth().height(100.dp))
                            }
                            
                            // Nutritional Info Section
                            Section(title = "Nutritional Information") {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories (kcal)") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("Protein (g)") }, modifier = Modifier.weight(1f))
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Carbs (g)") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Fat (g)") }, modifier = Modifier.weight(1f))
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    OutlinedTextField(value = fiber, onValueChange = { fiber = it }, label = { Text("Fiber (g)") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = sugar, onValueChange = { sugar = it }, label = { Text("Sugar (g)") }, modifier = Modifier.weight(1f))
                                }
                            }
                            
                            // Ingredients Section
                            Section(title = "Ingredients") {
                                ingredients.forEachIndexed { index, ingredient ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                        OutlinedTextField(value = ingredient.name, onValueChange = { newName -> ingredients = ingredients.toMutableList().also { it[index] = ingredient.copy(name = newName) } }, label = { Text("Name") }, modifier = Modifier.weight(2.5f))
                                        OutlinedTextField(value = ingredient.quantity.toString(), onValueChange = { newQty -> ingredients = ingredients.toMutableList().also { it[index] = ingredient.copy(quantity = newQty.toDoubleOrNull() ?: 0.0) } }, label = { Text("Qty") }, modifier = Modifier.weight(1f))
                                        OutlinedTextField(value = ingredient.unit, onValueChange = { newUnit -> ingredients = ingredients.toMutableList().also { it[index] = ingredient.copy(unit = newUnit) } }, label = { Text("Unit") }, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { ingredients = ingredients.toMutableList().also { it.removeAt(index) } }) {
                                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove Ingredient", tint = MaterialTheme.colors.error)
                                        }
                                    }
                                }
                                Button(onClick = { ingredients = ingredients + Ingredient(name = "", quantity = 0.0, unit = "") }, modifier = Modifier.align(Alignment.End)) { Text("Add Ingredient") }
                            }

                            // Instructions Section
                            Section(title = "Instructions") {
                                instructions.forEachIndexed { index, instruction ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                        Text("${index + 1}.", style = MaterialTheme.typography.h6)
                                        OutlinedTextField(value = instruction.description, onValueChange = { newDesc -> instructions = instructions.toMutableList().also { it[index] = instruction.copy(description = newDesc) } }, label = { Text("Step description") }, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { instructions = instructions.toMutableList().also { it.removeAt(index) }.mapIndexed { i, inst -> inst.copy(stepNumber = i + 1) } }) {
                                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove Instruction", tint = MaterialTheme.colors.error)
                                        }
                                    }
                                }
                                Button(onClick = { val newStepNumber = (instructions.lastOrNull()?.stepNumber ?: 0) + 1; instructions = instructions + Instruction(stepNumber = newStepNumber, description = "") }, modifier = Modifier.align(Alignment.End)) { Text("Add Step") }
                            }

                            // Scheduling Section
                            Section(title = "Scheduling") {
                                // Meal Tag Dropdown
                                var expanded by remember { mutableStateOf(false) }
                                val mealTags = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedMealTag,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Meal Tag") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                        ) {
                            mealTags.forEach { tag ->
                                            DropdownMenuItem(
                                                onClick = {
                                                    selectedMealTag = tag
                                                    expanded = false
                                                }
                                            ) {
                                                Text(text = tag)
                                            }
                                        }
                                    }
                                }

                                // Day of Week Chips
                                Text("Assign to Days", style = MaterialTheme.typography.body1, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DayOfWeek.entries.forEach { day ->
                                        val isSelected = selectedDays.contains(day.value)
                                FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedDays = if (isSelected) {
                                                    selectedDays - day.value
                                                } else {
                                                    selectedDays + day.value
                                                }
                                            },
                                            content = { Text(day.name.take(3).uppercase()) },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                            } else {
                                                null
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                        VerticalScrollbar(modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(), adapter = rememberScrollbarAdapter(scrollState))
                    }

                    // Footer with actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colors.surface)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val recipe = Recipe(
                                        id = recipeToEdit?.id ?: "",
                                        name = name.trim(),
                                        note = note.takeIf { it.isNotBlank() },
                                        protein = protein.toDoubleOrNull(),
                                        calories = calories.toDoubleOrNull(),
                                        carbs = carbs.toDoubleOrNull(),
                                        fat = fat.toDoubleOrNull(),
                                        fiber = fiber.toDoubleOrNull(),
                                        sugar = sugar.toDoubleOrNull(),
                                        days = selectedDays,
                                        mealTag = selectedMealTag,
                                        ingredients = ingredients.filter { it.name.isNotBlank() },
                                        instructions = instructions.filter { it.description.isNotBlank() }
                                    )
                                    onSave(recipe)
                                    onDismiss()
                                } else {
                                    nameError = "Recipe name is required"
                                }
                            }
                        ) {
                            Text(if (recipeToEdit == null) "Create Recipe" else "Save Changes")
                        }
                    }
                }
            }
        }
    }
} 

