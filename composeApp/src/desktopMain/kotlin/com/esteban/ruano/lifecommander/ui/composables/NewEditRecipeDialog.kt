package ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.esteban.ruano.lifecommander.models.Recipe
import java.time.DayOfWeek

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewEditRecipeDialog(
    recipeToEdit: Recipe?,
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: (Recipe) -> Unit
) {
    if (!show) return

    var name by remember { mutableStateOf(recipeToEdit?.name ?: "") }
    var note by remember { mutableStateOf(recipeToEdit?.note ?: "") }
    var protein by remember { mutableStateOf(recipeToEdit?.protein?.toString() ?: "") }
    var selectedDay by remember { mutableStateOf(recipeToEdit?.day ?: 1) }
    var selectedMealTag by remember { mutableStateOf(recipeToEdit?.mealTag ?: "BREAKFAST") }
    var nameError by remember { mutableStateOf<String?>(null) }

    val mealTags = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
    val daysOfWeek = DayOfWeek.values()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .size(600.dp, 700.dp)
                .padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.primary,
                    elevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (recipeToEdit == null) "Add Recipe" else "Edit Recipe",
                                style = MaterialTheme.typography.h5,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onPrimary
                            )
                            Text(
                                text = if (recipeToEdit == null) "Create a new recipe" else "Update recipe details",
                                style = MaterialTheme.typography.subtitle2,
                                color = MaterialTheme.colors.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Recipe Name
                    Column {
                        Text(
                            text = "Recipe Name *",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { 
                                name = it
                                nameError = if (it.isBlank()) "Recipe name is required" else null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter recipe name") },
                            isError = nameError != null,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colors.primary,
                                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                            )
                        )
                        if (nameError != null) {
                            Text(
                                text = nameError!!,
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.error,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }

                    // Notes
                    Column {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Add notes about the recipe") },
                            minLines = 3,
                            maxLines = 5,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colors.primary,
                                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                            )
                        )
                    }

                    // Protein
                    Column {
                        Text(
                            text = "Protein (g)",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = protein,
                            onValueChange = { protein = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter protein content") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colors.primary,
                                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                            )
                        )
                    }

                    // Day Selection
                    Column {
                        Text(
                            text = "Day of Week",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            daysOfWeek.forEach { day ->
                                FilterChip(
                                    selected = selectedDay == day.value,
                                    onClick = { selectedDay = day.value },
                                    content = { Text(day.name.take(3)) },
                                )
                            }
                        }
                    }

                    // Meal Tag
                    Column {
                        Text(
                            text = "Meal Type",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mealTags.forEach { tag ->
                                FilterChip(
                                    selected = selectedMealTag == tag,
                                    onClick = { selectedMealTag = tag },
                                    content = { Text(tag.capitalize()) },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val recipe = Recipe(
                                        id = recipeToEdit?.id ?: "",
                                        name = name.trim(),
                                        note = note.takeIf { it.isNotBlank() },
                                        protein = protein.toDoubleOrNull(),
                                        day = selectedDay,
                                        mealTag = selectedMealTag
                                    )
                                    onSave(recipe)
                                } else {
                                    nameError = "Recipe name is required"
                                }
                            },
                            enabled = name.isNotBlank()
                        ) {
                            Text(if (recipeToEdit == null) "Add Recipe" else "Update Recipe")
                        }
                    }
                }
            }
        }
    }
} 